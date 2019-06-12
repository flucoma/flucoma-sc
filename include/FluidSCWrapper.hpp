#pragma once

#include "SCBufferAdaptor.hpp"
#include <clients/common/FluidBaseClient.hpp>
#include <clients/common/Result.hpp>
#include <data/FluidTensor.hpp>
#include <data/TensorTypes.hpp>

#include <SC_PlugIn.hpp>

#include <tuple>
#include <type_traits>
#include <utility>
#include <vector>

namespace fluid {
namespace client {

template <typename Client>
class FluidSCWrapper;

namespace impl {

// Iterate over kr/ir inputs via callbacks from params object
struct FloatControlsIter
{
  FloatControlsIter(float **vals, size_t N)
  : mValues(vals)
  , mSize(N)
  {}
    
  float next() { return mCount >= mSize ? 0 : *mValues[mCount++]; }
    
  void reset(float **vals)
  {
    mValues = vals;
    mCount  = 0;
  }
    
  size_t size() const noexcept { return mSize; }
    
private:
  float **mValues;
  size_t  mSize;
  size_t  mCount{0};
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
// Real Time Processor

template <typename Client, class Wrapper>
class RealTime : public SCUnit
{
  using HostVector = FluidTensorView<float, 1>;
  using ParamSetType = typename Client::ParamSetType;
    
  //  using Client     = typename Wrapper::ClientType;

public:
  static void setup(InterfaceTable *ft, const char *name)
  {
    registerUnit<Wrapper>(ft, name);
    ft->fDefineUnitCmd(name, "latency", doLatency);
  }

  static void doLatency(Unit *unit, sc_msg_iter*)
  {
    float l[]{static_cast<float>(static_cast<Wrapper *>(unit)->mClient.latency())};
    auto  ft = Wrapper::getInterfaceTable();

    std::stringstream ss;
    ss << '/' << Wrapper::getName() << "_latency";
    std::cout << ss.str() << '\n';
    ft->fSendNodeReply(&unit->mParent->mNode, -1, ss.str().c_str(), 1, l);
  }
    
  RealTime()
    : mControlsIterator{mInBuf + mSpecialIndex + 1,static_cast<size_t>(static_cast<ptrdiff_t>(mNumInputs) - mSpecialIndex - 1)}
    , mParams{Wrapper::Client::getParameterDescriptors()}
    , mClient{Wrapper::setParams(mParams,mWorld->mVerbosity > 0, mWorld, mControlsIterator,true)}
  {}

  void init()
  {
    assert(!(mClient.audioChannelsOut() > 0 && mClient.controlChannelsOut() > 0) &&
           "Client can't have both audio and control outputs");

    //If we don't the number of arguments we expect, the language side code is probably the wrong version
    //set plugin to no-op, squawk, and bail;
    if(mControlsIterator.size() != Client::getParameterDescriptors().count())
    {
      mCalcFunc = Wrapper::getInterfaceTable()->fClearUnitOutputs;
      std::cout << "ERROR: " << Wrapper::getName() << " wrong number of arguments. Expected "
                << Client::getParameterDescriptors().count() << ", got " << mControlsIterator.size()
                << ". Your .sc file and binary plugin might be different versions." << std::endl;
      return;
    }

    mClient.sampleRate(fullSampleRate());
    mInputConnections.reserve(mClient.audioChannelsIn());
    mOutputConnections.reserve(mClient.audioChannelsOut());
    mAudioInputs.reserve(mClient.audioChannelsIn());
    mOutputs.reserve(std::max(mClient.audioChannelsOut(), mClient.controlChannelsOut()));

    for (int i = 0; i < static_cast<int>(mClient.audioChannelsIn()); ++i)
    {
      mInputConnections.emplace_back(isAudioRateIn(i));
      mAudioInputs.emplace_back(nullptr, 0, 0);
    }

    for (int i = 0; i < static_cast<int>(mClient.audioChannelsOut()); ++i)
    {
      mOutputConnections.emplace_back(true);
      mOutputs.emplace_back(nullptr, 0, 0);
    }

    for (int i = 0; i < static_cast<int>(mClient.controlChannelsOut()); ++i) { mOutputs.emplace_back(nullptr, 0, 0); }
  
    mCalcFunc = make_calc_function<RealTime, &RealTime::next>();
//    set_calc_function<RealTime, &RealTime::next>();    
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
    
    
    
  }

  void next(int)
  {
    mControlsIterator.reset(mInBuf + 1); //mClient.audioChannelsIn());
    Wrapper::setParams(mParams, mWorld->mVerbosity > 0, mWorld, mControlsIterator); // forward on inputs N + audio inputs as params
    mParams.constrainParameterValues(); 
    const Unit *unit = this;
    for (size_t i = 0; i < mClient.audioChannelsIn(); ++i)
    {
      if (mInputConnections[i]) mAudioInputs[i].reset(IN(i), 0, fullBufferSize());
    }
    for (size_t i = 0; i < mClient.audioChannelsOut(); ++i)
    {
      if (mOutputConnections[i]) mOutputs[i].reset(out(static_cast<int>(i)), 0, fullBufferSize());
    }
    for (size_t i = 0; i < mClient.controlChannelsOut(); ++i) { mOutputs[i].reset(out(static_cast<int>(i)), 0, 1); }
    mClient.process(mAudioInputs, mOutputs);
  }

private:
  std::vector<bool>       mInputConnections;
  std::vector<bool>       mOutputConnections;
  std::vector<HostVector> mAudioInputs;
  std::vector<HostVector> mOutputs;
  FloatControlsIter       mControlsIterator;

protected:
  ParamSetType  mParams;
  Client        mClient;
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/// Non Real Time Processor

template <typename Client, typename Wrapper>
class NonRealTime
{
  using ParamSetType = typename Client::ParamSetType;

public:
  static void setup(InterfaceTable *ft, const char *name) { DefinePlugInCmd(name, launch, nullptr); }

  NonRealTime(World* w, sc_msg_iter* args)
      : mParams{Client::getParameterDescriptors()}
      , mClient{Wrapper::setParams(mParams, false, w, args)}
  {}

  void init(){};

  static void launch(World *world, void */*inUserData*/, struct sc_msg_iter *args, void *replyAddr)
  {

    if (args->tags && ((std::string{args->tags}.size() - 1) != Client::getParameterDescriptors().count()))
    {
          std::cout << "ERROR: " << Wrapper::getName() << " wrong number of arguments. Expected "
                << Client::getParameterDescriptors().count() << ", got " << (std::string{args->tags}.size() - 1)
                << ". Your .sc file and binary plugin might be different versions." << std::endl;
      return;
    }

    Wrapper *w = new Wrapper(
        world, args); // this has to be on the heap, because it doesn't get destroyed until the async command is done

    Result result = validateParameters(w);
    if (!result.ok())
    {
        std::cout << "ERROR: " << Wrapper::getName() << ": " << result.message().c_str() << std::endl;
        delete w;
          return;
    }
    
    size_t            msgSize = args->getbsize();
    std::vector<char> completionMessage(msgSize);
    //    char * completionMsgData = 0;
    if (msgSize) { args->getb(completionMessage.data(), msgSize); }

    world->ft->fDoAsynchronousCommand(world, replyAddr, Wrapper::getName(), w, process, exchangeBuffers, tidyUp, destroy,
                                      static_cast<int>(msgSize), completionMessage.data());
  }

  static bool process(World *world, void *data) { return static_cast<Wrapper *>(data)->process(world); }
  static bool exchangeBuffers(World *world, void *data) { return static_cast<Wrapper *>(data)->exchangeBuffers(world); }
  static bool tidyUp(World *world, void *data) { return static_cast<Wrapper *>(data)->tidyUp(world); }
  static void destroy(World *, void *data) { delete static_cast<Wrapper *>(data); }

protected:
  ParamSetType  mParams;
  Client        mClient;

private:
    
  static Result validateParameters(NonRealTime *w)
  {
    auto results = w->mParams.constrainParameterValues();
    for (auto &r : results)
    {
      if (!r.ok()) return r;
    }
    return {};
  }

  bool process(World *)
  {
    Result r = mClient.process();

    if (!r.ok())
    {
      std::cout << "ERROR: " << Wrapper::getName() << ": " << r.message().c_str() << '\n';
      return false;
    }

    return true;
  }

  bool exchangeBuffers(World *world)
  {
    mParams.template forEachParamType<BufferT, AssignBuffer>(world);
    return true;
  }

  bool tidyUp(World *)
  {
    mParams.template forEachParamType<BufferT, CleanUpBuffer>();
    return true;
  }

  template <size_t N, typename T>
  struct AssignBuffer
  {
    void operator()(const typename BufferT::type &p, World *w)
    {
      if (auto b = static_cast<SCBufferAdaptor *>(p.get())) b->assignToRT(w);
    }
  };

  template <size_t N, typename T>
  struct CleanUpBuffer
  {
    void operator()(const typename BufferT::type &p)
    {
      if (auto b = static_cast<SCBufferAdaptor *>(p.get())) b->cleanUp();
    }
  };

  char *      mCompletionMessage = nullptr;
  void *      mReplyAddr         = nullptr;
  const char *mName              = nullptr;
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
/// An impossible monstrosty
template <typename Client, typename Wrapper>
class NonRealTimeAndRealTime : public RealTime<Client, Wrapper>, public NonRealTime<Client, Wrapper>
{
  static void setup(InterfaceTable *ft, const char *name)
  {
    RealTime<Client,Wrapper>::setup(ft, name);
    NonRealTime<Client,Wrapper>::setup(ft, name);
  }
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
// Template Specialisations for NRT/RT

template <typename Client, typename Wrapper, typename NRT, typename RT>
class FluidSCWrapperImpl;

template <typename Client, typename Wrapper>
class FluidSCWrapperImpl<Client, Wrapper, std::true_type, std::false_type>
    : public NonRealTime<Client, Wrapper>
{
public:
  FluidSCWrapperImpl(World* w, sc_msg_iter *args): NonRealTime<Client, Wrapper>(w,args){};
};

template <typename Client, typename Wrapper>
class FluidSCWrapperImpl<Client, Wrapper, std::false_type, std::true_type> : public RealTime<Client, Wrapper>
{};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
// Make base class(es), full of CRTP mixin goodness
template <typename Client>
using FluidSCWrapperBase = FluidSCWrapperImpl<Client, FluidSCWrapper<Client>, isNonRealTime<Client>, isRealTime<Client>>;

} // namespace impl

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///The main wrapper
template <typename C>
class FluidSCWrapper : public impl::FluidSCWrapperBase<C>
{
  using FloatControlsIter = impl::FloatControlsIter;
  
  // Iterate over arguments in sc_msg_iter, via callbacks from params object
  
  template <typename ArgType, size_t N, typename T>
  struct Setter
  {
    static constexpr size_t argSize = C::getParameterDescriptors().template get<N>().fixedSize;
    
    auto fromArgs(World *, FloatControlsIter& args, LongT::type, int) { return args.next(); }
    auto fromArgs(World *, FloatControlsIter& args, FloatT::type, int) { return args.next(); }
    auto fromArgs(World *, sc_msg_iter* args, LongT::type, int defVal) { return args->geti(defVal); }
    auto fromArgs(World *, sc_msg_iter* args, FloatT::type, int) { return args->getf(); }

    auto fromArgs(World *w, ArgType args, BufferT::type, int)
    {
      typename LongT::type bufnum = static_cast<LongT::type>(fromArgs(w, args, LongT::type(), -1));
      return BufferT::type(bufnum >= 0 ? new SCBufferAdaptor(bufnum, w) : nullptr);
    }
    
    typename T::type operator()(World *w, ArgType args)
    {
      ParamLiteralConvertor<T, argSize> a;
      using LiteralType = typename ParamLiteralConvertor<T, argSize>::LiteralType;
      
      for (size_t i = 0; i < argSize; i++)
        a[i] = static_cast<LiteralType>(fromArgs(w, args, a[0], 0));
      
      return a.value();
    }
  };
  
  template <size_t N, typename T>
  using ArgumentSetter = Setter<sc_msg_iter*, N, T>;
  
  template <size_t N, typename T>
  using ControlSetter = Setter<FloatControlsIter&, N, T>;
  
public:
  using Client = C;
  using ParameterSetType = typename C::ParamSetType;

  FluidSCWrapper() // mParams{*getParamDescriptors()}, //impl::FluidSCWrapperBase<Client,Params>()
  {
    impl::FluidSCWrapperBase<Client>::init();
  }

  FluidSCWrapper(World* w, sc_msg_iter *args): impl::FluidSCWrapperBase<Client>(w,args)
  {
    impl::FluidSCWrapperBase<Client>::init();
  }

  static const char *getName(const char *setName = nullptr)
  {
    static const char *name = nullptr;
    return (name = setName ? setName : name);
  }

  static InterfaceTable *getInterfaceTable(InterfaceTable *setTable = nullptr)
  {
    static InterfaceTable *ft = nullptr;
    return (ft = setTable ? setTable : ft);
  }

  static void setup(InterfaceTable *ft, const char *name)
  {
    getName(name);
    getInterfaceTable(ft);
    impl::FluidSCWrapperBase<Client>::setup(ft, name);
  }

  static auto& setParams(ParameterSetType& p, bool verbose, World* world, FloatControlsIter& inputs, bool constrain = false)
  {
    //We won't even try and set params if the arguments don't match 
    if(inputs.size() == C::getParameterDescriptors().count())
    {
        p.template setParameterValues<ControlSetter>(verbose, world, inputs);
        if (constrain)p.constrainParameterValues();
    }
    return p;
  }

  static auto& setParams(ParameterSetType& p, bool verbose, World* world, sc_msg_iter *args)
  {
      p.template setParameterValues<ArgumentSetter>(verbose,world, args);
     return p;
  }
};

template <template<typename T> class Client>
void makeSCWrapper(const char *name, InterfaceTable *ft)
{
  FluidSCWrapper<Client<float>>::setup(ft, name);
}

} // namespace client
} // namespace fluid
