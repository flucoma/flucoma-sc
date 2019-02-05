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

template <typename Client> class FluidSCWrapper;

namespace impl {

template <typename Client, typename T, size_t N> struct Setter;
template <size_t N, typename T> struct ArgumentGetter;
template <size_t N, typename T> struct ControlGetter;
template <typename T> using msg_iter_method = T (sc_msg_iter::*)(T);

template <size_t N, typename T, msg_iter_method<T> Method> struct GetArgument
{
  T operator()(World* w, sc_msg_iter *args)
  {
    T r = (args->*Method)(T{0});
    return r;
  }
};


struct FloatControlsIter
{
  FloatControlsIter(float** vals, size_t N):mValues(vals), mSize(N) {}
  
  float next()
  {
    assert(mCount + 1 < mSize);
    return *mValues[mCount++];
  }
  
  void reset(float** vals)
  {
    mValues = vals;
    mCount = 0;
  }
  
  private:
    float** mValues;
    size_t mSize;
    size_t mCount{0};
};

template <size_t N, typename T> struct GetControl
{
  T operator()(World*, FloatControlsIter& controls) { return controls.next(); }
};

template <size_t N> struct ArgumentGetter<N, FloatT> : public GetArgument<N, float, &sc_msg_iter::getf>
{
//  ArgumentGetter() { std::cout << "FloatT @ " << N << '\n'; }
};

template <size_t N> struct ArgumentGetter<N, LongT> : public GetArgument<N, int32, &sc_msg_iter::geti>
{
//    ArgumentGetter() { std::cout << "LongT @ " << N << '\n'; }

};

template <size_t N> struct ArgumentGetter<N, EnumT> : public GetArgument<N, int32, &sc_msg_iter::geti>
{
//  ArgumentGetter() { std::cout << "Enum @ " << N << '\n'; }

};

template <size_t N> struct ArgumentGetter<N, BufferT>
{
//  ArgumentGetter() { std::cout << "Buffer @ " << N << '\n'; }
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



template <size_t N, typename T> struct ControlGetter : public GetControl<N, typename T::type>
{};


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


template<typename Client, typename Args, template <size_t,typename> class Fetcher>
struct ClientFactory
{
  static Client create(World* world,  Args* args)
  {
    return createImpl(world, args, FixedParamIndices{});
  }

private:
  using FixedParamIndices = typename Client::FixedParams;
  template<size_t N>
  using ThisParamType = typename Client::template ParamDescriptorTypeAt<N>;
  
  template<size_t...Is>
  static Client createImpl(World* world,  Args* args, std::index_sequence<Is...>)
  {
    return Client{Fetcher<Is, ThisParamType<Is>>{}(world,*args)...};
  }
};

//template <size_t N, typename 

template <typename Client,class Wrapper> class RealTime : public SCUnit
{
  using HostVector = FluidTensorView<float, 1>;
  //  using Client     = typename Wrapper::ClientType;

public:
  static void setup(InterfaceTable *ft, const char *name) { registerUnit<Wrapper>(ft, name); }
  
  RealTime():
    mControlsIterator{mInBuf + mSpecialIndex + 1,mNumInputs - mSpecialIndex - 1},
    mClient{ClientFactory<Client,FloatControlsIter,ControlGetter>::create(mWorld,&mControlsIterator)}
  {}

  void init()
  {
    assert(!(mClient.audioChannelsOut() > 0 && mClient.controlChannelsOut() > 0) && "Client can't have both audio and control outputs");

    mInputConnections.reserve(mClient.audioChannelsIn());
    mOutputConnections.reserve(mClient.audioChannelsOut());
    mAudioInputs.reserve(mClient.audioChannelsIn());
    mAudioOutputs.reserve(mClient.audioChannelsOut());
    mControlOutputs.reserve(mClient.controlChannelsOut());
//    mControlOutputData.resize(mClient.controlChannelsOut(),)
    
    for (int i = 0; i < mClient.audioChannelsIn(); ++i)
    {
      mInputConnections.emplace_back(isAudioRateIn(i));
      mAudioInputs.emplace_back(nullptr, 0, 0);
    }

    for (int i = 0; i < mClient.audioChannelsOut(); ++i)
    {
      mOutputConnections.emplace_back(true);
      mAudioOutputs.emplace_back(nullptr, 0, 0);
    }

//    for (int i = 0; i < mClient.controlChannelsOut(); ++i)
//    {
//      mControlOutputs.emplace_back()
//    }

    set_calc_function<RealTime, &RealTime::next>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
  }

  void next(int n)
  {
    Wrapper *w = static_cast<Wrapper *>(this);
//    auto &client = w->client();
    mControlsIterator.reset(mInBuf + mClient.audioChannelsIn());
    w->setParams( mWorld->mVerbosity > 0, mWorld,mControlsIterator); // forward on inputs N + audio inputs as params
    const Unit *unit = this;
    for (int i = 0; i < mClient.audioChannelsIn(); ++i)
    {
      if (mInputConnections[i]) mAudioInputs[i].reset(IN(i), 0, fullBufferSize());
    }
    for (int i = 0; i < mClient.audioChannelsOut(); ++i)
    {
      if (mOutputConnections[i]) mAudioOutputs[i].reset(out(i), 0, fullBufferSize());
    }
    for(int i = 0; i < mClient.controlChannelsOut();++i)
    {
      if(mOutputConnections[i]) mControlOutputs[i].reset(out(i),0,1);
    }
    mClient.process(mAudioInputs, mAudioOutputs);
  }
private:
  std::vector<bool>       mInputConnections;
  std::vector<bool>       mOutputConnections;
  std::vector<HostVector> mAudioInputs;
  std::vector<HostVector> mAudioOutputs;
  std::vector<HostVector> mControlOutputs;
  FloatControlsIter       mControlsIterator;
protected:
  Client mClient;

};

template <typename Client, typename Wrapper> class NonRealTime
{
public:
  static void setup(InterfaceTable *ft, const char *name) { DefinePlugInCmd(name, launch, nullptr); }

  NonRealTime(World *world,sc_msg_iter *args):
    mClient{ClientFactory<Client, sc_msg_iter, ArgumentGetter>::create(world,args)}
  {}

  void init(){};

  static void launch(World *world, void *inUserData, struct sc_msg_iter *args, void *replyAddr)
  {
    Wrapper *w = new Wrapper(world,args); //this has to be on the heap, because it doesn't get destoryed until the async command is done
    w->parseBuffers(w, world, args);
    int argsPosition = args->count;
    auto argsRdPos   = args->rdpos;
    Result result = validateParameters(w, world, args);
    if (!result.ok())
    {
      std::cout << "FluCoMa Error " << Wrapper::getName() << ": " << result.message().c_str();
      return;
    }
    args->count = argsPosition;
    args->rdpos = argsRdPos;
    w->setParams(false, world, args);
    
    size_t msgSize  = args->getbsize();
    char * completionMsgData = 0;
    if (msgSize)
    {
      completionMsgData = (char *) world->ft->fRTAlloc(world, msgSize);
      args->getb(completionMsgData, msgSize);
    }
    world->ft->fDoAsynchronousCommand(world, replyAddr, Wrapper::getName(), w, process, exchangeBuffers, tidyUp, destroy,
                                      msgSize, completionMsgData);
  }

  static bool process(World *world, void *data) { return static_cast<Wrapper *>(data)->process(world); }
  static bool exchangeBuffers(World *world, void *data) { return static_cast<Wrapper *>(data)->exchangeBuffers(world); }
  static bool tidyUp(World *world, void *data) { return static_cast<Wrapper *>(data)->tidyUp(world); }
  static void destroy(World *world, void *data) { delete static_cast<Wrapper *>(data); }

protected:
  Client mClient;

private:
  static Result validateParameters(NonRealTime *w, World* world, sc_msg_iter *args)
  {
    auto &c       = w->mClient;
    auto  results = c.template checkParameterValues<ArgumentGetter>(world, args);
    for (auto &r : results)
    {
      std::cout << r.message() << '\n';
      if (!r.ok()) return r;
    }
    return {};
  }

  void parseBuffers(Wrapper *w, World *world, sc_msg_iter *args)
  {
    auto &c = mClient;

    mBuffersIn.reserve(c.audioBuffersIn());
    mInputs.reserve(c.audioBuffersIn());
    mBuffersOut.reserve(c.audioBuffersOut());
    mOutputs.reserve(c.audioBuffersOut());

    for (int i = 0; i < c.audioBuffersIn(); i++)
    {
      mBuffersIn.emplace_back(args->geti(0), world);
      mInputs.emplace_back();
      mInputs[i].buffer     = &mBuffersIn[i];
      mInputs[i].startFrame = args->geti(0);
      mInputs[i].nFrames    = args->geti(0);
      mInputs[i].startChan  = args->geti(0);
      mInputs[i].nChans     = args->geti(0);
    }

    for (int i = 0; i < c.audioBuffersOut(); i++)
    {
      mBuffersOut.emplace_back(args->geti(0), world);
      mOutputs.emplace_back();
      mOutputs[i].buffer = &mBuffersOut[i];
    }
  }

  bool process(World *world)
  {
    Result r = mClient.process(mInputs, mOutputs);
    
    if(!r.ok())
    {
      std::cout << "FluCoMa Error " << Wrapper::getName() << ": " << r.message().c_str();
      return false; 
    }
    
    return true;
  }

  bool exchangeBuffers(World *world)
  {
    
    mClient.template forEachParamType<BufferT,AssignBuffer>(world);
    for (auto &b : mBuffersOut) b.assignToRT(world);
    return true;
  }

  bool tidyUp(World *world)
  {
    for (auto &b : mBuffersIn) b.cleanUp();
    for (auto &b : mBuffersOut) b.cleanUp();
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

  std::vector<SCBufferAdaptor>   mBuffersIn;
  std::vector<SCBufferAdaptor>   mBuffersOut;
  std::vector<BufferProcessSpec> mInputs;
  std::vector<BufferProcessSpec> mOutputs;
  void *                         mReplyAddr;
  const char *                   mName;
};

template <typename Client, typename Wrapper> class NonRealTimeAndRealTime : public RealTime<Client,Wrapper>, public NonRealTime<Client,Wrapper>
{
  static void setup(InterfaceTable *ft, const char *name)
  {
    RealTime<Client,Wrapper>::setup(ft, name);
    NonRealTime<Client,Wrapper>::setup(ft, name);
  }
};

// Template Specialisations for NRT/RT

template <typename Client, typename Wrapper, typename NRT, typename RT> class FluidSCWrapperImpl;

template <typename Client, typename Wrapper> class FluidSCWrapperImpl<Client, Wrapper, std::true_type, std::false_type> : public NonRealTime<Client, Wrapper>
{
public:
  FluidSCWrapperImpl(World* w, sc_msg_iter *args): NonRealTime<Client, Wrapper>(w,args){};
};

template <typename Client, typename Wrapper> class FluidSCWrapperImpl<Client, Wrapper, std::false_type, std::true_type> : public RealTime<Client, Wrapper>
{};

// Make base class(es), full of CRTP mixin goodness
template <typename Client>
using FluidSCWrapperBase = FluidSCWrapperImpl<Client, FluidSCWrapper<Client>, isNonRealTime<Client>, isRealTime<Client>>;

} // namespace impl

template <typename Client> class FluidSCWrapper : public impl::FluidSCWrapperBase<Client>
{

public:
  using ClientType = Client;

  FluidSCWrapper() { impl::FluidSCWrapperBase<Client>::init(); }

  FluidSCWrapper(World* w, sc_msg_iter *args): impl::FluidSCWrapperBase<Client>(w,args)
    { impl::FluidSCWrapperBase<Client>::init(); }


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

  auto setParams(bool verbose, World* world, impl::FloatControlsIter& inputs)
  {
    return impl::FluidSCWrapperBase<Client>::mClient.template setParameterValues<impl::ControlGetter>(verbose, world, inputs);
  }

  auto setParams(bool verbose, World* world, sc_msg_iter *args)
  {
    return impl::FluidSCWrapperBase<Client>::mClient.template setParameterValues<impl::ArgumentGetter>(verbose,world, args);
  }

//  Client &client() { return mClient; }

//private:
//  Client mClient;
};

template <typename Client> void makeSCWrapper(InterfaceTable *ft, const char *name)
{
  FluidSCWrapper<Client>::setup(ft, name);
}

} // namespace client
} // namespace fluid
