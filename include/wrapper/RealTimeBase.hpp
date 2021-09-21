#pragma once

#include <SC_PlugIn.hpp>

namespace fluid {
namespace client {
namespace impl {


template <typename Client, class Wrapper>
struct RealTimeBase
{
  using IOMapFn = void (RealTimeBase::*)(SCUnit&, Client&);
  using HostVector = FluidTensorView<float, 1>;
  using Params = typename Client::ParamSetType;
  template <typename T, bool>
  struct doExpectedCount;

  template <typename T>
  struct doExpectedCount<T, false>
  {
    static void count(const T& d, FloatControlsIter& c, Result& status)
    {
      if (!status.ok()) return;

      if (c.remain())
      {
        index statedSize = d.fixedSize;

        if (c.remain() < statedSize)
          status = {Result::Status::kError, "Ran out of arguments at ", d.name};

        // fastforward
        for (index i = 0; i < statedSize; ++i) c.next();
      }
    }
  };

  template <typename T>
  struct doExpectedCount<T, true>
  {
    static void count(const T& d, FloatControlsIter& c, Result& status)
    {
      if (!status.ok()) return;

      if (c.remain())
      {
        index statedSize = 1;

        if (c.remain() < statedSize)
          status = {Result::Status::kError, "Ran out of arguments at ", d.name};

        // fastforward
        for (index i = 0; i < statedSize; ++i) c.next();
      }
    }
  };
  template <size_t N, typename T>
  struct ExpectedCount
  {
    void operator()(const T& descriptor, FloatControlsIter& c, Result& status)
    {
      doExpectedCount<T, IsSharedClientRef<typename T::type>::value>::count(
          descriptor, c, status);
    }
  };

  Result expectedSize(FloatControlsIter& controls)
  {
    if (controls.size() < Client::getParameterDescriptors().count())
    {
      return {Result::Status::kError, "Fewer parameters than exepected. Got ",
              controls.size(), "expect at least",
              Client::getParameterDescriptors().count()};
    }

    Result countScan;
    Client::getParameterDescriptors().template iterate<ExpectedCount>(
        std::forward<FloatControlsIter&>(controls),
        std::forward<Result&>(countScan));
    return countScan;
  }

  void init(SCUnit& unit, Client& client, FloatControlsIter& controls)
  {
    assert(!(client.audioChannelsOut() > 0 &&
             client.controlChannelsOut().count > 0) &&
           "Client can't have both audio and control outputs");
    client.sampleRate(unit.fullSampleRate());
    mInputConnections.reserve(asUnsigned(client.audioChannelsIn()));
    mOutputConnections.reserve(asUnsigned(client.audioChannelsOut()));

    Result r;
    if (!(r = expectedSize(controls)).ok())
    {
      std::cout << "ERROR: " << Wrapper::getName()
                << " wrong number of arguments." << r.message() << std::endl;
      return;
    }

    if (client.audioChannelsIn())
    {
      mAudioInputs.reserve(asUnsigned(client.audioChannelsIn()));
      for (index i = 0; i < client.audioChannelsIn(); ++i)
      {
        mInputConnections.emplace_back(unit.isAudioRateIn(static_cast<int>(i)));
        mAudioInputs.emplace_back(nullptr, 0, 0);
      }
      mInputMapper = &RealTimeBase::mapAudioInputs;
    }
    else if (client.controlChannelsIn())
    {
      mControlInputBuffer.resize(unit.mSpecialIndex + 1);
      mAudioInputs.emplace_back(mControlInputBuffer);
      mInputMapper = &RealTimeBase::mapControlInputs;
    }

    index outputSize = client.controlChannelsOut().size > 0
                           ? std::max(client.audioChannelsOut(),
                                      client.controlChannelsOut().size)
                           : unit.mSpecialIndex + 1;
    mOutputs.reserve(asUnsigned(outputSize));

    if (client.audioChannelsOut())
    {
      for (index i = 0; i < client.audioChannelsOut(); ++i)
      {
        mOutputConnections.emplace_back(true);
        mOutputs.emplace_back(nullptr, 0, 0);
      }
      
      mOutMapperPre = &RealTimeBase::mapAudioOutputs;
      mOutMapperPost = &RealTimeBase::mapNoOp;
    }
    else
    {
      index totalControlOutputs =
          client.controlChannelsOut().count * outputSize;
      mControlOutputBuffer.resize(totalControlOutputs);
      for (index i = 0; i < client.controlChannelsOut().count; ++i)
      {
        mOutputs.emplace_back(
            mControlOutputBuffer(fluid::Slice(i * outputSize, outputSize)));
      }

      mOutMapperPre = &RealTimeBase::mapNoOp;
      mOutMapperPost = &RealTimeBase::mapControlOutputs;
    }
  }

  void mapNoOp(SCUnit&, Client&) {}

  void mapAudioInputs(SCUnit& unit, Client& client)
  {
    for (index i = 0; i < client.audioChannelsIn(); ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      if (mInputConnections[asUnsigned(i)])
        mAudioInputs[asUnsigned(i)].reset(
            const_cast<float*>(unit.in(static_cast<int>(i))), 0,
            unit.fullBufferSize());
    }
  }

  void mapAudioOutputs(SCUnit& unit, Client& client)
  {
    for (index i = 0; i < client.audioChannelsOut(); ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      if (mOutputConnections[asUnsigned(i)])
        mOutputs[asUnsigned(i)].reset(unit.out(static_cast<int>(i)), 0,
                                      unit.fullBufferSize());
    }
  }

  void mapControlInputs(SCUnit& unit, Client& client)
  {
    for (index i = 0; i < unit.mSpecialIndex + 1; ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      mControlInputBuffer[asUnsigned(i)] = unit.in0(static_cast<int>(i));
    }
  }

  void mapControlOutputs(SCUnit& unit, Client& client)
  {
    for (index i = 0; i < mControlOutputBuffer.size(); ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      unit.out0(static_cast<int>(i)) = mControlOutputBuffer(i);
    }
  }

  void next(SCUnit& unit, Client& client, Params& params,
            FloatControlsIter& controls, bool updateParams = true)
  {
    bool trig =
        IsModel_t<Client>::value ? !mPrevTrig && unit.in0(0) > 0 : false;

    mPrevTrig = trig;

    if (updateParams)
    {
      Wrapper::setParams(&unit, params, controls);
      params.constrainParameterValuesRT(nullptr);
    }

    (this->*mInputMapper)(unit, client);
    (this->*mOutMapperPre)(unit, client);
    client.process(mAudioInputs, mOutputs, mContext);
    (this->*mOutMapperPost)(unit, client);
  }

private:
  std::vector<bool>       mInputConnections;
  std::vector<bool>       mOutputConnections;
  std::vector<HostVector> mAudioInputs;
  std::vector<HostVector> mOutputs;
  FluidTensor<float, 1>   mControlInputBuffer;
  FluidTensor<float, 1>   mControlOutputBuffer;
  FluidContext            mContext;
  bool                    mPrevTrig;
  IOMapFn                 mInputMapper;
  IOMapFn                 mOutMapperPre;
  IOMapFn                 mOutMapperPost;
};
} // namespace impl
} // namespace client
} // namespace fluid
