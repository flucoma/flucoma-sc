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

template <typename Client, typename Params> class FluidSCWrapper;

namespace impl {

template <typename Client, typename T, size_t N> struct Setter;
template <size_t N, typename T> struct ArgumentGetter;
template <size_t N, typename T> struct ControlGetter;
template <typename T> using msg_iter_method = T (sc_msg_iter::*)(T);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Iterate over kr/ir inputs via callbacks from params object
struct FloatControlsIter
{
  FloatControlsIter(float** vals, size_t N):mValues(vals), mSize(N) {}
  
  float next()
  {
    return mCount >= mSize ? 0 : *mValues[mCount++];
  }

  void reset(float** vals)
  {
    mValues = vals;
    mCount = 0;
  }
  
  size_t size() const noexcept { return mSize; }
  
  private:
    float** mValues;
    size_t mSize;
    size_t mCount{0};
};

//General case
template <size_t N, typename T> struct GetControl
{
  T operator()(World*, FloatControlsIter& controls) { return controls.next(); }
};

template <size_t N, typename T> struct ControlGetter : public GetControl<N, typename T::type>
{};

//Specializations
template <size_t N> struct ControlGetter<N, BufferT>
{
  auto operator() (World* w, FloatControlsIter& iter)
  {
    typename LongT::type bufnum = iter.next();
    return std::unique_ptr<BufferAdaptor>(bufnum >= 0  ? new SCBufferAdaptor(bufnum,w): nullptr);
  }
};

template<size_t N>
struct ControlGetter<N,FloatPairsArrayT>
{
  typename FloatPairsArrayT::type operator()(World*, FloatControlsIter& iter)
  {
    return {{iter.next(),iter.next()},{iter.next(),iter.next()}};
  }
};

template<size_t N>
struct ControlGetter<N,FFTParamsT>
{
  typename FFTParamsT::type operator()(World*, FloatControlsIter& iter)
  {
    return {static_cast<long>(iter.next()),static_cast<long>(iter.next()),static_cast<long>(iter.next())};
  }
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Iterate over arguments in sc_msg_iter, via callbacks from params object

template <size_t N, typename T, msg_iter_method<T> Method> struct GetArgument
{
  T operator()(World* w, sc_msg_iter *args)
  {
    T r = (args->*Method)(T{0});
    return r;
  }
};

//General cases
template <size_t N> struct ArgumentGetter<N, FloatT> : public GetArgument<N, float, &sc_msg_iter::getf>
{};

template <size_t N> struct ArgumentGetter<N, LongT> : public GetArgument<N, int32, &sc_msg_iter::geti>
{};

template <size_t N> struct ArgumentGetter<N, EnumT> : public GetArgument<N, int32, &sc_msg_iter::geti>
{};

//Specializations
template <size_t N> struct ArgumentGetter<N, BufferT>
{
  auto operator() (World* w, sc_msg_iter *args)
  {
    typename LongT::type bufnum = args->geti(-1);
    return std::unique_ptr<BufferAdaptor>(bufnum >= 0 ? new SCBufferAdaptor(bufnum,w) : nullptr);
  }
};

template <size_t N> struct ArgumentGetter<N, FloatPairsArrayT>
{
  typename FloatPairsArrayT::type operator()(World* w, sc_msg_iter *args)
  {
    return {{args->getf(),args->getf()},{args->getf(),args->getf()}};
  }
};

template <size_t N> struct ArgumentGetter<N, FFTParamsT>
{
  typename FFTParamsT::type operator()(World* w, sc_msg_iter *args)
  {
    return {args->geti(),args->geti(),args->geti()};
  }
};


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Real Time Processor

template <typename Client,class Wrapper, class Params> class RealTime : public SCUnit
{
  using HostVector = FluidTensorView<float, 1>;
  //  using Client     = typename Wrapper::ClientType;

public:
  static void setup(InterfaceTable *ft, const char *name)
  {
    registerUnit<Wrapper>(ft, name);
    ft->fDefineUnitCmd(name,"latency",doLatency);
  }
  
  static void doLatency(Unit *unit, sc_msg_iter *args)
  {
    float l[] {static_cast<float>(static_cast<Wrapper*>(unit)->mClient.latency())};
    auto ft = Wrapper::getInterfaceTable();
    
    std::stringstream ss;
    ss << '/' << Wrapper::getName() << "_latency";
    std::cout << ss.str() << '\n';
    ft->fSendNodeReply(&unit->mParent->mNode,-1,ss.str().c_str() , 1, l);
  }
  
  RealTime():
    mControlsIterator{mInBuf + mSpecialIndex + 1,mNumInputs - mSpecialIndex - 1},
    mParams{*Wrapper::getParamDescriptors()},
    mClient{Wrapper::setParams(mParams,mWorld->mVerbosity > 0, mWorld, mControlsIterator)}
  {}

  void init()
  {
    assert(!(mClient.audioChannelsOut() > 0 && mClient.controlChannelsOut() > 0) && "Client can't have both audio and control outputs");

    //If we don't the number of arguments we expect, the language side code is probably the wrong version
    //set plugin to no-op, squawk, and bail;
    if(mControlsIterator.size() != Wrapper::getParamDescriptors()->count())
    {
      mCalcFunc = Wrapper::getInterfaceTable()->fClearUnitOutputs;
      std::cout << "ERROR: " << Wrapper::getName() <<
            " wrong number of arguments. Expected " << Wrapper::getParamDescriptors()->count() <<
            ", got " << mControlsIterator.size() << ". Your .sc file and binary plugin might be different versions." << std::endl;
      return;
    }
    
    mInputConnections.reserve(mClient.audioChannelsIn());
    mOutputConnections.reserve(mClient.audioChannelsOut());
    mAudioInputs.reserve(mClient.audioChannelsIn());
    mOutputs.reserve(std::max(mClient.audioChannelsOut(),mClient.controlChannelsOut()));
    
    for (int i = 0; i < mClient.audioChannelsIn(); ++i)
    {
      mInputConnections.emplace_back(isAudioRateIn(i));
      mAudioInputs.emplace_back(nullptr, 0, 0);
    }

    for (int i = 0; i < mClient.audioChannelsOut(); ++i)
    {
      mOutputConnections.emplace_back(true);
      mOutputs.emplace_back(nullptr, 0, 0);
    }

    for (int i = 0; i < mClient.controlChannelsOut(); ++i)
    {
      mOutputs.emplace_back(nullptr, 0, 0);
    }

    set_calc_function<RealTime, &RealTime::next>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
  }

  void next(int n)
  {
    mControlsIterator.reset(mInBuf + 1); //mClient.audioChannelsIn());
    Wrapper::setParams(mParams,mWorld->mVerbosity > 0, mWorld,mControlsIterator); // forward on inputs N + audio inputs as params
    const Unit *unit = this;
    for (int i = 0; i < mClient.audioChannelsIn(); ++i)
    {
      if (mInputConnections[i]) mAudioInputs[i].reset(IN(i), 0, fullBufferSize());
    }
    for (int i = 0; i < mClient.audioChannelsOut(); ++i)
    {
      if (mOutputConnections[i]) mOutputs[i].reset(out(i), 0, fullBufferSize());
    }
    for(int i = 0; i < mClient.controlChannelsOut();++i)
    {
       mOutputs[i].reset(out(i),0,1);
    }
    mClient.process(mAudioInputs, mOutputs);
  }
private:
  std::vector<bool>       mInputConnections;
  std::vector<bool>       mOutputConnections;
  std::vector<HostVector> mAudioInputs;
  std::vector<HostVector> mOutputs;
  FloatControlsIter       mControlsIterator;
protected:
  ParameterSet<Params> mParams;
  Client mClient;
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// Non Real Time Processor
template <typename Client, typename Wrapper, typename Params> class NonRealTime
{
public:
  static void setup(InterfaceTable *ft, const char *name) { DefinePlugInCmd(name, launch, nullptr); }

  NonRealTime(World *world,sc_msg_iter *args):
    mParams{*Wrapper::getParamDescriptors()},
    mClient{mParams}
  {}

  void init(){};

  static void launch(World *world, void *inUserData, struct sc_msg_iter *args, void *replyAddr)
  {

    
    if(args->tags && ((std::string{args->tags}.size() - 1) !=  Wrapper::getParamDescriptors()->count()))
    {
      std::cout << "ERROR: " << Wrapper::getName() <<
            " wrong number of arguments. Expected " << Wrapper::getParamDescriptors()->count() <<
            ", got " << (std::string{args->tags}.size() - 1) << ". Your .sc file and binary plugin might be different versions." << std::endl;
      return;
    }
  
    Wrapper *w = new Wrapper(world,args); //this has to be on the heap, because it doesn't get destoryed until the async command is done
    
    int argsPosition = args->count;
    auto argsRdPos   = args->rdpos;
    Result result = validateParameters(w, world, args);
    if (!result.ok())
    {
      std::cout << "ERROR: " << Wrapper::getName() << ": " << result.message().c_str() << std::endl;
      delete w;
      return;
    }
    args->count = argsPosition;
    args->rdpos = argsRdPos;
    Wrapper::setParams(w->mParams,false, world, args);
    
    size_t msgSize  = args->getbsize();
    std::vector<char> completionMessage(msgSize);
//    char * completionMsgData = 0;
    if (msgSize)
    {
      args->getb(completionMessage.data(), msgSize);      
    }

    world->ft->fDoAsynchronousCommand(world, replyAddr, Wrapper::getName(), w, process, exchangeBuffers, tidyUp, destroy,msgSize, completionMessage.data());
  }

  static bool process(World *world, void *data) { return static_cast<Wrapper *>(data)->process(world); }
  static bool exchangeBuffers(World *world, void *data) { return static_cast<Wrapper *>(data)->exchangeBuffers(world); }
  static bool tidyUp(World *world, void *data) { return static_cast<Wrapper *>(data)->tidyUp(world); }
  static void destroy(World *world, void *data)
  {
    
//    void* c = static_cast<Wrapper *>(data)->mCompletionMessage;
//    if(c) world->ft->fRTFree(world,c);
    delete static_cast<Wrapper *>(data);
  }

protected:
  ParameterSet<Params> mParams;
  Client mClient;
private:
  static Result validateParameters(NonRealTime *w, World* world, sc_msg_iter *args)
  {
    auto  results = w->mParams.template checkParameterValues<ArgumentGetter>(world, args);
    for (auto &r : results)
    {
      if (!r.ok()) return r;
    }
    return {};
  }

  bool process(World *world)
  {
    Result r = mClient.process();///mInputs, mOutputs);
    
    if(!r.ok())
    {
      std::cout << "ERROR: " << Wrapper::getName() << ": " << r.message().c_str();
      return false; 
    }
    
    return true;
  }

  bool exchangeBuffers(World *world)
  {
    mParams.template forEachParamType<BufferT,AssignBuffer>(world);
//    for (auto &b : mBuffersOut) b.assignToRT(world);
    return true;
  }

  bool tidyUp(World *world)
  {
//    for (auto &b : mBuffersIn) b.cleanUp();
//    for (auto &b : mBuffersOut) b.cleanUp()
    mParams.template forEachParamType<BufferT,CleanUpBuffer>(); 
    return true;
  }

  template<size_t N,typename T>
  struct AssignBuffer
  {
    void operator()(typename BufferT::type& p, World* w)
    {
      if(auto b = static_cast<SCBufferAdaptor*>(p.get()))
       b->assignToRT(w);
    }
  };
  
  template<size_t N,typename T>
  struct CleanUpBuffer
  {
    void operator()(typename BufferT::type& p)
    {
      if(auto b = static_cast<SCBufferAdaptor*>(p.get()))
         b->cleanUp();
    }
  };

//  std::vector<SCBufferAdaptor>   mBuffersIn;
//  std::vector<SCBufferAdaptor>   mBuffersOut;
//  std::vector<BufferProcessSpec> mInputs;
//  std::vector<BufferProcessSpec> mOutputs;
  char *        mCompletionMessage = nullptr;
  void *        mReplyAddr = nullptr;
  const char *  mName = nullptr;
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// An impossible monstrosty
template <typename Client, typename Wrapper, typename Params> class NonRealTimeAndRealTime : public RealTime<Client,Wrapper, Params>, public NonRealTime<Client,Wrapper, Params>
{
  static void setup(InterfaceTable *ft, const char *name)
  {
    RealTime<Client,Wrapper,Params >::setup(ft, name);
    NonRealTime<Client,Wrapper, Params>::setup(ft, name);
  }
};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Template Specialisations for NRT/RT

template <typename Client, typename Wrapper, typename Params, typename NRT, typename RT> class FluidSCWrapperImpl;

template <typename Client, typename Wrapper, typename Params> class FluidSCWrapperImpl<Client, Wrapper, Params, std::true_type, std::false_type> : public NonRealTime<Client, Wrapper, Params>
{
public:
  FluidSCWrapperImpl(World* w, sc_msg_iter *args): NonRealTime<Client, Wrapper, Params>(w,args){};
};

template <typename Client, typename Wrapper, typename Params> class FluidSCWrapperImpl<Client, Wrapper,Params, std::false_type, std::true_type> : public RealTime<Client, Wrapper, Params>
{};

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Make base class(es), full of CRTP mixin goodness
template <typename Client,typename Params>
using FluidSCWrapperBase = FluidSCWrapperImpl<Client, FluidSCWrapper<Client, Params>,Params, isNonRealTime<Client>, isRealTime<Client>>;

} // namespace impl

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///The main wrapper
template <typename C, typename P> class FluidSCWrapper : public impl::FluidSCWrapperBase<C,P>
{

public:
  using Client = C;
  using Params = P;

  FluidSCWrapper() //mParams{*getParamDescriptors()}, //impl::FluidSCWrapperBase<Client,Params>()
  { impl::FluidSCWrapperBase<Client,Params>::init(); }

  FluidSCWrapper(World* w, sc_msg_iter *args): impl::FluidSCWrapperBase<Client, Params>(w,args)
    { impl::FluidSCWrapperBase<Client, Params>::init(); }


  static const char *getName(const char *setName = nullptr)
  {
    static const char *name = nullptr;
    return (name = setName ? setName : name);
  }
  
  static Params *getParamDescriptors(Params *setParams = nullptr)
  {
    static Params* descriptors = nullptr;
    return (descriptors = setParams ? setParams : descriptors);
  }

  static InterfaceTable *getInterfaceTable(InterfaceTable *setTable = nullptr)
  {
    static InterfaceTable *ft = nullptr;
    return (ft = setTable ? setTable : ft);
  }

  static void setup(Params& p, InterfaceTable *ft, const char *name)
  {
    getName(name);
    getInterfaceTable(ft);
    getParamDescriptors(&p);
    impl::FluidSCWrapperBase<Client, Params>::setup(ft, name);
  }

  template<typename ParameterSet>
  static auto& setParams(ParameterSet& p, bool verbose, World* world, impl::FloatControlsIter& inputs)
  {
    //We won't even try and set params if the arguments don't match 
    if(inputs.size() == getParamDescriptors()->count())
      p.template setParameterValues<impl::ControlGetter>(verbose, world, inputs);
    return p;
  }

  template<typename ParameterSet>
  static auto& setParams(ParameterSet& p, bool verbose, World* world, sc_msg_iter *args)
  {
     p.template setParameterValues<impl::ArgumentGetter>(verbose,world, args);
     return p;
  }

//  impl::ParameterSet<Params> mParams;

//  Client &client() { return mClient; }
//
//private:
//  Client mClient;
};

template <template <typename...> class Client,typename...Rest,typename Params>
void makeSCWrapper(const char *name, Params& params, InterfaceTable *ft)
{
  FluidSCWrapper<Client<ParameterSet<Params>,Rest...>, Params>::setup(params, ft, name);
}

} // namespace client
} // namespace fluid
