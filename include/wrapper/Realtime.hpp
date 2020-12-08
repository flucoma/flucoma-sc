#pragma once

#include "ArgsFromClient.hpp"
#include "Meta.hpp"
#include "RealTimeBase.hpp"
#include <clients/common/FluidBaseClient.hpp>
#include <SC_PlugIn.hpp>

// Real Time Processor
namespace fluid {
namespace client {  
namespace impl {
  
template <typename Client, class Wrapper>
class RealTime : public SCUnit
{
  
  using Delegate = impl::RealTimeBase<Client,Wrapper>;
  using Params = typename Client::ParamSetType;
  
public:

  // static index ControlOffset(Unit* unit) { return Delegate::ControlOffset(unit); }
  // static index ControlSize(Unit* unit)   { return Delegate::ControlSize(unit); }

  static index ControlOffset(Unit* unit) { return unit->mSpecialIndex + 1; }
  static index ControlSize(Unit* unit) 
  { 
    return static_cast<index>(unit->mNumInputs) 
                                - unit->mSpecialIndex 
                                - 1  
                                - (IsModel_t<Client>::value ? 1 : 0); 
  }

  static void setup(InterfaceTable* ft, const char* name)
  {
    ft->fDefineUnitCmd(name, "latency", doLatency);
    registerUnit<RealTime>(ft,name);
  }

  static void doLatency(Unit* unit, sc_msg_iter*)
  {
    float l[]{
         static_cast<float>(static_cast<RealTime*>(unit)->mClient.latency())
    };
    auto ft = Wrapper::getInterfaceTable();

    std::stringstream ss;
    ss << '/' << Wrapper::getName() << "_latency";
    std::cout << ss.str() << std::endl;
    ft->fSendNodeReply(&unit->mParent->mNode, -1, ss.str().c_str(), 1, l);
  }

  RealTime()
    : mControls{mInBuf + ControlOffset(this),ControlSize(this)},
      mClient{Wrapper::setParams(this, mParams, mControls)}
  {
    init();
  }

  void init()
  {
//    auto& client = mClient;
    mDelegate.init(*this,mClient,mControls);
    mCalcFunc = make_calc_function<RealTime, &RealTime::next>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);

    // assert(
    //     !(client.audioChannelsOut() > 0 && client.controlChannelsOut() > 0) &&
    //     "Client can't have both audio and control outputs");
    // 
    // Result r;
    // if(!(r = expectedSize(mWrapper->mControlsIterator)).ok())
    // {
    //   mCalcFunc = Wrapper::getInterfaceTable()->fClearUnitOutputs;
    //   std::cout
    //       << "ERROR: " << Wrapper::getName()
    //       << " wrong number of arguments."
    //       << r.message()
    //       << std::endl;
    //   return;
    // }
    // 
    // mWrapper->mControlsIterator.reset(mInBuf + mSpecialIndex + 1);
    // 
    // client.sampleRate(fullSampleRate());
    // mInputConnections.reserve(asUnsigned(client.audioChannelsIn()));
    // mOutputConnections.reserve(asUnsigned(client.audioChannelsOut()));
    // mAudioInputs.reserve(asUnsigned(client.audioChannelsIn()));
    // mOutputs.reserve(asUnsigned(
    //     std::max(client.audioChannelsOut(), client.controlChannelsOut())));
    // 
    // for (index i = 0; i < client.audioChannelsIn(); ++i)
    // {
    //   mInputConnections.emplace_back(isAudioRateIn(static_cast<int>(i)));
    //   mAudioInputs.emplace_back(nullptr, 0, 0);
    // }
    // 
    // for (index i = 0; i < client.audioChannelsOut(); ++i)
    // {
    //   mOutputConnections.emplace_back(true);
    //   mOutputs.emplace_back(nullptr, 0, 0);
    // }
    // 
    // for (index i = 0; i < client.controlChannelsOut(); ++i)
    // { mOutputs.emplace_back(nullptr, 0, 0); }
    // 
    // mCalcFunc = make_calc_function<RealTime, &RealTime::next>();
    // Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
  }

  void next(int)
  {
//    auto& client = mWrapper->client();
//    auto& params = mWrapper->params();
//    const Unit* unit = this;
    mControls.reset(mInBuf + ControlOffset(this)); 
    mDelegate.next(*this,mClient,mParams,mControls);
//     bool trig =  IsModel_t<Client>::value ? !mPrevTrig  && in0(0) > 0 : false;
//     bool shouldProcess = IsModel_t<Client>::value ? trig : true;
//     mPrevTrig = trig;
// 
//     if(shouldProcess)
//     {
//       mWrapper->mControlsIterator.reset(mInBuf + mSpecialIndex +
//                               1); // mClient.audioChannelsIn());
//       Wrapper::setParams(mWrapper,
//           params, mWrapper->mControlsIterator); // forward on inputs N + audio inputs as params
//       params.constrainParameterValues();
//     }
//       for (index i = 0; i < client.audioChannelsIn(); ++i)
//       {
//         if (mInputConnections[asUnsigned(i)])
//         { mAudioInputs[asUnsigned(i)].reset(IN(i), 0, fullBufferSize()); }
//       }
//       for (index i = 0; i < client.audioChannelsOut(); ++i)
//       {
//         assert(i <= std::numeric_limits<int>::max());
//         if (mOutputConnections[asUnsigned(i)])
//           mOutputs[asUnsigned(i)].reset(out(static_cast<int>(i)), 0,
//                                         fullBufferSize());
//       }
//       for (index i = 0; i < client.controlChannelsOut(); ++i)
//       {
//         assert(i <= std::numeric_limits<int>::max());
//         mOutputs[asUnsigned(i)].reset(out(static_cast<int>(i)), 0, 1);
//       }
//       client.process(mAudioInputs, mOutputs, mContext);
// //    }
  }
private:
  Delegate mDelegate;
  FloatControlsIter   mControls;
  Params mParams{Client::getParameterDescriptors()};
  Client mClient;
  // std::vector<bool>       mInputConnections;
  // std::vector<bool>       mOutputConnections;
  // std::vector<HostVector> mAudioInputs;
  // std::vector<HostVector> mOutputs;
  // FluidContext            mContext;
  
  Wrapper* mWrapper{static_cast<Wrapper*>(this)};
  // bool                    mPrevTrig;
};

}
}
}
