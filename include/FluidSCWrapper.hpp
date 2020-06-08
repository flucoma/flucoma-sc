/*
Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)
Copyright 2017-2019 University of Huddersfield.
Licensed under the BSD-3 License.
See license.md file in the project root for full license information.
This project has received funding from the European Research Council (ERC)
under the European Union’s Horizon 2020 research and innovation programme
(grant agreement No 725899).
*/

#pragma once

#include "SCBufferAdaptor.hpp"
#include <clients/common/FluidBaseClient.hpp>
#include <clients/nrt/FluidSharedInstanceAdaptor.hpp>
#include <clients/common/FluidNRTClientWrapper.hpp>
#include <clients/common/Result.hpp>
#include <clients/common/SharedClientUtils.hpp>
#include <data/FluidTensor.hpp>
#include <data/TensorTypes.hpp>
#include <FluidVersion.hpp>
#include <SC_PlugIn.hpp>
#include <atomic>
#include <algorithm>
#include <memory>
#include <string>
#include <tuple>
#include <type_traits>
#include <unordered_set>
#include <utility>
#include <vector>


namespace fluid {
namespace client {

template <typename Client>
class FluidSCWrapper;


template<typename Client>
struct WrapperState
{
    typename Client::ParamSetType params;
    Client       client;
    Node*        mNode;
    std::atomic<bool> mCancelled{false};
    std::atomic<bool> mJobDone{false};
    std::atomic<bool> mHasTriggered{false};
    std::atomic<bool> mSynchronous{false};
    std::atomic<bool> mInNRT{false};
    std::atomic<bool> mNodeAlive{true};
    Result       mResult{};
};

namespace impl {

template <size_t N, typename T>
struct AssignBuffer
{
  void operator()(const typename BufferT::type& p, World* w)
  {
    if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->assignToRT(w);
  }
};

template <size_t N, typename T>
struct CleanUpBuffer
{
  void operator()(const typename BufferT::type& p)
  {
    if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->cleanUp();
  }
};


// Iterate over kr/ir inputs via callbacks from params object
struct FloatControlsIter
{
  FloatControlsIter(float** vals, index N) : mValues(vals), mSize(N) {}

  float next() { return mCount >= mSize ? 0 : *mValues[mCount++]; }

  void reset(float** vals)
  {
    mValues = vals;
    mCount = 0;
  }

  index size() const noexcept { return mSize; }
  index remain() { return mSize - mCount; }

private:
  float** mValues;
  index   mSize;
  index   mCount{0};
};

////////////////////////////////////////////////////////////////////////////////

// Real Time Processor

template <typename Client, class Wrapper>
class RealTime : public SCUnit
{
  using HostVector = FluidTensorView<float, 1>;
  using ParamSetType = typename Client::ParamSetType;

  template<typename T, bool>
  struct doExpectedCount;
  
  template<typename T>
  struct doExpectedCount<T, false>
  {
    static void count(const T& d,FloatControlsIter& c,Result& status)
    {
      if(!status.ok()) return;
      
      if(c.remain())
      {
        index statedSize = d.fixedSize;
        
        if(c.remain() < statedSize)
            status =  {Result::Status::kError,"Ran out of arguments at ", d.name};
        
        //fastforward
        for(index i=0; i < statedSize; ++i) c.next();
        
      }
    }
  };

  template<typename T>
  struct doExpectedCount<T, true>
  {
    static void count(const T& d,FloatControlsIter& c,Result& status)
    {
      if(!status.ok()) return;
      
      if(c.remain())
      {
        index statedSize = static_cast<index>(c.next());
      
        if(c.remain() < statedSize)
            status =  {Result::Status::kError,"Ran out of arguments at ", d.name};
        
        //fastforward
        for(index i=0; i < statedSize; ++i) c.next();
        
      }
    }
  };


  template<size_t N, typename T>
  struct ExpectedCount{
    void operator ()(const T& descriptor,FloatControlsIter& c, Result& status)
    {
      doExpectedCount<T,IsSharedClientRef<typename T::type>::value>::count(descriptor,c,status);
    }
  };
  
  Result expectedSize(FloatControlsIter& controls)
  {
    if(controls.size() < Client::getParameterDescriptors().count())
    {
      return {Result::Status::kError,"Fewer parameters than exepected. Got ", controls.size(), "expect at least", Client::getParameterDescriptors().count()};
    }
    
    Result countScan;
    Client::getParameterDescriptors().template iterate<ExpectedCount>(
        std::forward<FloatControlsIter&>(mWrapper->mControlsIterator),
        std::forward<Result&>(countScan));
    return countScan;
  }

public:

  static index ControlOffset(Unit* unit) { return unit->mSpecialIndex + 1; }
  static index ControlSize(Unit* unit) { return static_cast<index>(unit->mNumInputs) - unit->mSpecialIndex - 1; }

  static void setup(InterfaceTable* ft, const char* name)
  {
    ft->fDefineUnitCmd(name, "latency", doLatency);
  }

  static void doLatency(Unit* unit, sc_msg_iter*)
  {
    float l[]{
        static_cast<float>(static_cast<Wrapper*>(unit)->client().latency())
    };
    auto ft = Wrapper::getInterfaceTable();

    std::stringstream ss;
    ss << '/' << Wrapper::getName() << "_latency";
    std::cout << ss.str() << std::endl;
    ft->fSendNodeReply(&unit->mParent->mNode, -1, ss.str().c_str(), 1, l);
  }

  RealTime()
  {}

  void init()
  {
    
    auto& client =static_cast<Wrapper*>(this)->client();
    assert(
        !(client.audioChannelsOut() > 0 && client.controlChannelsOut() > 0) &&
        "Client can't have both audio and control outputs");

    Result r;
    if(!(r = expectedSize(mWrapper->mControlsIterator)).ok())
    {
      mCalcFunc = Wrapper::getInterfaceTable()->fClearUnitOutputs;
      std::cout
          << "ERROR: " << Wrapper::getName()
          << " wrong number of arguments."
          << r.message()
          << std::endl;
      return;
    }
    
    mWrapper->mControlsIterator.reset(mInBuf + mSpecialIndex + 1);

    client.sampleRate(fullSampleRate());
    mInputConnections.reserve(asUnsigned(client.audioChannelsIn()));
    mOutputConnections.reserve(asUnsigned(client.audioChannelsOut()));
    mAudioInputs.reserve(asUnsigned(client.audioChannelsIn()));
    mOutputs.reserve(asUnsigned(
        std::max(client.audioChannelsOut(), client.controlChannelsOut())));

    for (index i = 0; i < client.audioChannelsIn(); ++i)
    {
      mInputConnections.emplace_back(isAudioRateIn(static_cast<int>(i)));
      mAudioInputs.emplace_back(nullptr, 0, 0);
    }

    for (index i = 0; i < client.audioChannelsOut(); ++i)
    {
      mOutputConnections.emplace_back(true);
      mOutputs.emplace_back(nullptr, 0, 0);
    }

    for (index i = 0; i < client.controlChannelsOut(); ++i)
    { mOutputs.emplace_back(nullptr, 0, 0); }

    mCalcFunc = make_calc_function<RealTime, &RealTime::next>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
  }

  void next(int)
  {
  
    auto& client = mWrapper->client();
    auto& params = mWrapper->params();

    mWrapper->mControlsIterator.reset(mInBuf + mSpecialIndex +
                            1); // mClient.audioChannelsIn());
    Wrapper::setParams(mWrapper,
        params, mWrapper->mControlsIterator); // forward on inputs N + audio inputs as params
    params.constrainParameterValues();
    const Unit* unit = this;
    for (index i = 0; i < client.audioChannelsIn(); ++i)
    {
      if (mInputConnections[asUnsigned(i)])
      { mAudioInputs[asUnsigned(i)].reset(IN(i), 0, fullBufferSize()); }
    }
    for (index i = 0; i < client.audioChannelsOut(); ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      if (mOutputConnections[asUnsigned(i)])
        mOutputs[asUnsigned(i)].reset(out(static_cast<int>(i)), 0,
                                      fullBufferSize());
    }
    for (index i = 0; i < client.controlChannelsOut(); ++i)
    {
      assert(i <= std::numeric_limits<int>::max());
      mOutputs[asUnsigned(i)].reset(out(static_cast<int>(i)), 0, 1);
    }
    client.process(mAudioInputs, mOutputs, mContext);
  }
private:
  std::vector<bool>       mInputConnections;
  std::vector<bool>       mOutputConnections;
  std::vector<HostVector> mAudioInputs;
  std::vector<HostVector> mOutputs;
  FluidContext            mContext;
  Wrapper*                mWrapper{static_cast<Wrapper*>(this)};
};

////////////////////////////////////////////////////////////////////////////////

/// Non Real Time Processor
/// This is also a UGen, but the main action is delegated off to a worker
/// thread, via the NRT thread. The RT bit is there to allow us (a) to poll our
/// thread and (b) emit a kr progress update
template <typename Client, typename Wrapper>
class NonRealTime : public SCUnit
{
  using ParamSetType = typename Client::ParamSetType;
  using SharedState = std::shared_ptr<WrapperState<Client>>;
public:

  static index ControlOffset(Unit*) { return 0; }
  static index ControlSize(Unit* unit) { return index(unit->mNumInputs) - unit->mSpecialIndex - 2; }

  static void setup(InterfaceTable* ft, const char* name)
  {
    ft->fDefineUnitCmd(name, "cancel", doCancel);
//    ft->fDefineUnitCmd(
//        name, "queue_enabled", [](struct Unit* unit, struct sc_msg_iter* args) {
//          auto w = static_cast<Wrapper*>(unit);
//          w->mQueueEnabled = args->geti(0);
//          w->mFifoMsg.Set(
//              w->mWorld,
//              [](FifoMsg* f) {
//                auto w = static_cast<Wrapper*>(f->mData);
//                w->client().setQueueEnabled(w->mQueueEnabled);
//              },
//              nullptr, w);
//          Wrapper::getInterfaceTable()->fSendMsgFromRT(w->mWorld, w->mFifoMsg);
//        });
//    ft->fDefineUnitCmd(
//        name, "synchronous", [](struct Unit* unit, struct sc_msg_iter* args) {
//          auto w = static_cast<Wrapper*>(unit);
//          w->mSynchronous = args->geti(0);
//          w->mFifoMsg.Set(
//              w->mWorld,
//              [](FifoMsg* f) {
//                auto w = static_cast<Wrapper*>(f->mData);
//                w->client().setSynchronous(w->mSynchronous);
//              },
//              nullptr, w);
//          Wrapper::getInterfaceTable()->fSendMsgFromRT(w->mWorld, w->mFifoMsg);
//        });
  }

  /// Penultimate input is the trigger, final is blocking mode. Neither are
  /// params, so we skip them in the controlsIterator
  NonRealTime()
       : mSynchronous{mNumInputs > 2 ? (in0(int(mNumInputs) - 1) > 0) : false}
  {}

  ~NonRealTime()
  {
    auto state = mWrapper->client().state();
    if (state == ProcessState::kProcessing)
    {
      std::cout << Wrapper::getName() << ": Processing cancelled" << std::endl;
      Wrapper::getInterfaceTable()->fSendNodeReply(&mParent->mNode, 1, "/done",
                                                   0, nullptr);
    }
    // processing will be cancelled in ~NRTThreadAdaptor()
  }


  /// No option of not using a worker thread for now
  /// init() sets up the NRT process via the SC NRT thread, and then sets our
  /// UGen calc function going
  void init()
  {
    mWrapper->state()->mSynchronous = mSynchronous;
  
    mFifoMsg.Set(mWorld, initNRTJob, nullptr, this);
    
    // we want to poll thread roughly every 20ms
    checkThreadInterval = static_cast<index>(0.02 / controlDur());
    set_calc_function<NonRealTime, &NonRealTime::poll>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
  };

  /// The calc function. Checks to see if we've cancelled, spits out progress,
  /// launches tidy up when complete
  void poll(int)
  {
    out0(0) = mDone ? 1.0f : static_cast<float>(mWrapper->client().progress());
    
    index triggerInput = static_cast<index>(mInBuf[static_cast<index>(mNumInputs) - mSpecialIndex - 2][0]);
    bool  trigger = (mPreviousTrigger <= 0) && triggerInput > 0; 
    mPreviousTrigger = triggerInput;
    
    auto& sharedState = mWrapper->state();
    mWrapper->mDone = sharedState->mJobDone;
    if(trigger)
    {
      SharedState* statePtr = static_cast<SharedState*>(mWorld->ft->fRTAlloc(mWorld, sizeof(SharedState)));
      statePtr = new (statePtr) SharedState(sharedState);
      mFifoMsg.Set(mWorld, initNRTJob, nullptr, statePtr);
      mWorld->ft->fSendMsgFromRT(mWorld, mFifoMsg);
      return;
    }
    
    if (0 == pollCounter++ && !sharedState->mInNRT && sharedState->mHasTriggered)
    {
      sharedState->mInNRT = true;
      
      SharedState* statePtr = static_cast<SharedState*>(mWorld->ft->fRTAlloc(mWorld, sizeof(SharedState)));
      statePtr = new (statePtr) SharedState(sharedState);
      mWorld->ft->fDoAsynchronousCommand(mWorld, nullptr, Wrapper::getName(),
                                         statePtr, postProcess, exchangeBuffers,
                                         tidyUp, destroy, 0, nullptr);
    }
    pollCounter %= checkThreadInterval;
  }


  /// To be called on NRT thread. Validate parameters and commence processing in
  /// new thread
  static void initNRTJob(FifoMsg* f)
  {
    if(!f->mData) return; 
    auto w = static_cast<SharedState*>(f->mData);
    SharedState& s = *w;
    Result result = validateParameters(s->params);

    if (!result.ok())
    {
      std::cout << "ERROR: " << Wrapper::getName() << ": "
                << result.message().c_str() << std::endl;
      s->mInNRT = false;
      return;
    }
    s->client.setSynchronous(s->mSynchronous);
    result = s->client.enqueue(s->params);
    if (!result.ok())
    {
      std::cout << "ERROR: " << Wrapper::getName() << ": "
                << result.message().c_str() << std::endl;
      s->mInNRT = false;
      return;
    }
    
    s->mJobDone = false;
    s->mCancelled = false;
    s->mHasTriggered = true;
    s->mResult = s->client.process();
    s->mInNRT = false;
    w->~SharedState();
    f->mWorld->ft->fRTFree(f->mWorld,w);
  }

  /// Check result and report if bad
  static bool postProcess(World*, void* data)
  {
    
    if(!data) return false;
  
    auto&        w = *static_cast<SharedState*>(data);
    Result       r;
    w->mInNRT = true;
    ProcessState s = w->client.checkProgress(r);

    if(w->mSynchronous) r = w->mResult;

    if ((s == ProcessState::kDone || s == ProcessState::kDoneStillProcessing) ||
        (w->mSynchronous &&
         s == ProcessState::kNoProcess)) // I think this hinges on the fact that
                                         // when mSynchrous = true, this call
                                         // will always be behind process() on
                                         // the command FIFO, so we can assume
                                         // that if the state is kNoProcess, it
                                         // has run (vs never having run)
    {
      // Given that cancellation from the language now always happens by freeing
      // the synth, this block isn't reached normally. HOwever, if someone
      // cancels using u_cmd, this is what will fire
      if (r.status() == Result::Status::kCancelled)
      {
        std::cout << Wrapper::getName() << ": Processing cancelled"
                  << std::endl;
        w->mCancelled = true;
        w->mHasTriggered = false;
        w->mJobDone = true;
        return false;
      }

      if (!r.ok())
      {
        if(!w->mJobDone)
            std::cout << "ERROR: " << Wrapper::getName() << ": "
                  << r.message().c_str() << std::endl;
         w->mJobDone = true;
         w->mHasTriggered = false;
        return false;
      }
      w->mHasTriggered = false;
      w->mJobDone = true;
      return true;
    }
    return false;
  }

  /// swap NRT buffers back to RT-land
  static bool exchangeBuffers(World* world, void* data)
  {
    if(!data) return false;
    SharedState& s = *(static_cast<SharedState*>(data));
    if(!s->mNodeAlive) return false;
    s->params.template forEachParamType<BufferT, AssignBuffer>(world);
    // At this point, we can see if we're finished and let the language know (or
    // it can wait for the doneAction, but that takes extra time) use replyID to
    // convey status (0 = normal completion, 1 = cancelled)
    if (s->mJobDone && !s->mCancelled)
      world->ft->fSendNodeReply(s->mNode, 0, "/done", 0, nullptr);
    if (s->mCancelled)
      world->ft->fSendNodeReply(s->mNode, 1, "/done", 0, nullptr);
    return true;

  }
  /// Tidy up any temporary buffers
  static bool tidyUp(World* , void* data)
  {
    if(!data) return false;
    SharedState& s = *(static_cast<SharedState*>(data));
    s->params.template forEachParamType<BufferT, impl::CleanUpBuffer>();
    return true;
  }

  /// if we're properly done set the Unit done flag
  static void destroy(World* world, void* data)
  {
    if(!data) return;
    auto& s = *static_cast<SharedState*>(data);
    s->mInNRT = false;
    s.~SharedState();
    world->ft->fRTFree(world,data);
  }

  static void doCancel(Unit* unit, sc_msg_iter*)
  {
    static_cast<Wrapper*>(unit)->client().cancel();
  }

private:
  static Result validateParameters(ParamSetType& p)
  {
    auto results = p.constrainParameterValues();
    for (auto& r : results)
    {
      if (!r.ok()) return r;
    }
    return {};
  }

  template <size_t N, typename T>
  struct AssignBuffer
  {
    void operator()(const typename BufferT::type& p, World* w)
    {
      if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->assignToRT(w);
    }
  };

  template <size_t N, typename T>
  struct CleanUpBuffer
  {
    void operator()(const typename BufferT::type& p)
    {
      if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->cleanUp();
    }
  };

  FifoMsg           mFifoMsg;
  char*             mCompletionMessage = nullptr;
  void*             mReplyAddr = nullptr;
  const char*       mName = nullptr;
  index             checkThreadInterval;
  index             pollCounter{0};
  index             mPreviousTrigger{0};  

  bool         mSynchronous{true};
  Wrapper*     mWrapper{static_cast<Wrapper*>(this)};
  Result       mResult;
};

////////////////////////////////////////////////////////////////////////////////

/// An impossible monstrosty
template <typename Client, typename Wrapper>
class NonRealTimeAndRealTime : public RealTime<Client, Wrapper>,
                               public NonRealTime<Client, Wrapper>
{
  static void setup(InterfaceTable* ft, const char* name)
  {
    RealTime<Client, Wrapper>::setup(ft, name);
    NonRealTime<Client, Wrapper>::setup(ft, name);
  }
};

////////////////////////////////////////////////////////////////////////////////

//Discovery for clients that need persistent storage (Dataset and friends)

/// Named, shared clients already have a lookup table in their adaptor class
template <typename T>
struct IsPersistent
{
  using type = std::false_type;
};

//TODO: make less tied to current implementation
template <typename T>
struct IsPersistent<NRTThreadingAdaptor<NRTSharedInstanceAdaptor<T>>>
{
  using type = std::true_type;
};

template<typename T>
using IsPersistent_t = typename IsPersistent<T>::type;

/// Models don't, but still need to survive CMD-.
template<typename T>
struct IsModel
{
  using type = std::false_type;
};

template<typename T>
struct IsModel<NRTThreadingAdaptor<ClientWrapper<T>>>
{
  using type = typename ClientWrapper<T>::isModelObject;
};

template<typename T>
using IsModel_t = typename IsModel<T>::type;

template<typename,typename,typename, typename>
struct LifetimePolicy;

//template<typename Client, typename Wrapper>
//struct LifetimePolicy<Client, Wrapper,std::true_type, std::true_type>
//{
////  static_assert(false,"Shared Objecthood and Model Objecthood are not compatible");
//};

/// Default policy
template<typename Client, typename Wrapper>
struct LifetimePolicy<Client, Wrapper,std::false_type, std::false_type>
{
  static void constructClass(Unit* unit)
  {
      FloatControlsIter controlsReader{unit->mInBuf + Wrapper::ControlOffset(unit),Wrapper::ControlSize(unit)};
      auto params = typename Wrapper::ParamSetType{Client::getParameterDescriptors()};
      Wrapper::setParams(unit, params, controlsReader,true);
      Client client{params};
      new (static_cast<Wrapper*>(unit)) Wrapper(std::move(controlsReader), std::move(client), std::move(params));
  }
  static void destroyClass(Unit* unit) { static_cast<Wrapper*>(unit)->~Wrapper(); }
  static void setup(InterfaceTable*, const char*){}
};

/// Model objects
template<typename Client, typename Wrapper>
struct LifetimePolicy<Client, Wrapper,std::true_type, std::false_type>
{

  index uid;

  struct CacheRecord
  {
   typename Client::ParamSetType params{Client::getParameterDescriptors()};
   Client client{params};
   bool leased{false};
  };

  using Cache = std::unordered_map<index,CacheRecord>;

  static void constructClass(Unit* unit)
  {
      index uid  = static_cast<index>(unit->mInBuf[Wrapper::ControlOffset(unit)][0]);
      FloatControlsIter controlsReader{unit->mInBuf + 1 + Wrapper::ControlOffset(unit),Wrapper::ControlSize(unit)};
      auto& entry = mRegistry[uid];
      auto& client = entry.client;
      auto& params = entry.params;
      if(entry.leased) //if this happens, then the client has probably messed up
      {
        std::cout << "ERROR: ID " << uid << "is already being used by the cache" << std::endl;
        return;
      }
      Wrapper::setParams(unit, entry.params,controlsReader,true);
      new (static_cast<Wrapper*>(unit)) Wrapper{std::move(controlsReader),std::move(client),std::move(params)};
      static_cast<Wrapper*>(unit)->uid = uid;
      entry.leased = true;
  }

  static void destroyClass(Unit* unit)
  {
    auto wrapper = static_cast<Wrapper*>(unit);
    index uid = wrapper->uid;
    auto pos = mRegistry.find(uid);
    if( pos != mRegistry.end() )
    {
      //on cmd-. live to fight another day
      auto& entry = *pos;
      entry.second.client = std::move(wrapper->client());
      entry.second.params = std::move(wrapper->params());
      entry.second.leased = false;
    }
    wrapper->~Wrapper();
  }

  static void setup(InterfaceTable* ft, const char* name)
  {
      auto freeName = std::stringstream();
      freeName << "free" << name;
    
      ft->fDefinePlugInCmd(freeName.str().c_str(),
       [](World*,void*,sc_msg_iter* args, void*/*replyAddr*/)
       {
          auto objectID = args->geti();
          auto pos = mRegistry.find(objectID);
          if(pos != mRegistry.end()) mRegistry.erase(objectID);
       }, &mRegistry);
  }

private:
  static std::unordered_map<index,CacheRecord> mRegistry;
};

template<typename Client, typename Wrapper>
typename LifetimePolicy<Client, Wrapper, std::true_type, std::false_type>::Cache
                LifetimePolicy<Client, Wrapper, std::true_type, std::false_type>::mRegistry{};


/// Shared objects
template<typename Client, typename Wrapper>
struct LifetimePolicy<Client, Wrapper,std::false_type, std::true_type>
{

  template<typename> struct GetSharedType;

  template<typename T>
  struct GetSharedType<NRTThreadingAdaptor<NRTSharedInstanceAdaptor<T>>>
  {
    using type = NRTSharedInstanceAdaptor<T>;
  };

  using SharedType = typename GetSharedType<Client>::type;
  using ClientPointer = typename SharedType::ClientPointer;

  static void constructClass(Unit* unit)
  {

    FloatControlsIter controlsReader{unit->mInBuf + Wrapper::ControlOffset(unit),Wrapper::ControlSize(unit)};

    auto params = typename Client::ParamSetType{Client::getParameterDescriptors()};
    Wrapper::setParams(unit, params,controlsReader,true);
    
    auto& name = params.template get<0>();
    
    auto client = Client{params};
    auto clientRef = SharedType::lookup(name);

    auto pos = mRegistry.find(clientRef);
    if(pos == mRegistry.end()) mRegistry.emplace(clientRef);

    new (static_cast<Wrapper*>(unit)) Wrapper(std::move(controlsReader),std::move(client),std::move(params));

  }
  static void destroyClass(Unit* unit) { static_cast<Wrapper*>(unit)->~Wrapper(); }

  static void setup(InterfaceTable* ft, const char* name)
  {

      auto freeName = std::stringstream();
      freeName << "free" << name;
    
      ft->fDefinePlugInCmd(freeName.str().c_str(),
       [](World*,void*,sc_msg_iter* args, void* /*replyAddr*/)
       {
          auto objectName = std::string(args->gets());
          auto clientRef = SharedType::lookup(objectName);
          auto pos = mRegistry.find(clientRef);
          if(pos != mRegistry.end()) mRegistry.erase(clientRef);
       }, &mRegistry);
    
  }
private:
  static  ClientPointer getClientPointer(Wrapper* wrapper)
  {
    auto& params = wrapper->params();
    auto name = params.template get<0>();
    return  SharedType::lookup(name);
  }

  static std::unordered_set<ClientPointer> mRegistry;
};

template<typename Client, typename Wrapper>
std::unordered_set<typename LifetimePolicy<Client, Wrapper, std::false_type, std::true_type>::ClientPointer>
                LifetimePolicy<Client, Wrapper, std::false_type, std::true_type>::mRegistry{};


//// Template Specialisations for NRT/RT

template <typename Client, typename Wrapper, typename NRT, typename RT>
class FluidSCWrapperImpl;

template <typename Client, typename Wrapper>
class FluidSCWrapperImpl<Client, Wrapper, std::true_type, std::false_type>
    : public NonRealTime<Client, Wrapper>,
      public LifetimePolicy<Client,Wrapper,IsModel_t<Client>, IsPersistent_t<Client>>
{
public:
  void init(){
    NonRealTime<Client,Wrapper>::init();
  }
  static void setup(InterfaceTable* ft, const char* name)
  {
    NonRealTime<Client,Wrapper>::setup(ft,name);
    LifetimePolicy<Client,Wrapper,IsModel_t<Client>, IsPersistent_t<Client>>::setup(ft,name);
  }
};

template <typename Client, typename Wrapper>
class FluidSCWrapperImpl<Client, Wrapper, std::false_type, std::true_type>
    : public RealTime<Client, Wrapper>,
      public LifetimePolicy<Client,Wrapper,IsModel_t<Client>, IsPersistent_t<Client>>
{
public:
  void init(){
    RealTime<Client,Wrapper>::init();
  }
  static void setup(InterfaceTable* ft, const char* name)
  {
    RealTime<Client,Wrapper>::setup(ft,name);
    LifetimePolicy<Client,Wrapper,IsModel_t<Client>, IsPersistent_t<Client>>::setup(ft,name);
  }
};

////////////////////////////////////////////////////////////////////////////////

// Make base class(es), full of CRTP mixin goodness
template <typename Client>
using FluidSCWrapperBase = FluidSCWrapperImpl<Client, FluidSCWrapper<Client>,
                                              typename Client::isNonRealTime,
                                              typename Client::isRealTime>;

} // namespace impl

////////////////////////////////////////////////////////////////////////////////

/// The main wrapper
template <typename C>
class FluidSCWrapper : public impl::FluidSCWrapperBase<C>
{

  using FloatControlsIter = impl::FloatControlsIter;

  using SharedState = std::shared_ptr<WrapperState<C>>;
  //I would like to template these to something more scaleable, but baby steps
  friend class impl::RealTime<C,FluidSCWrapper>;
  friend class impl::NonRealTime<C,FluidSCWrapper>;

  template <typename ArgType>
  struct ParamReader
  {

    static const char* oscTagToString(char tag)
    {
      switch (tag)
      {
        case 'i': return "integer"; break;
        case 'f': return "float"; break;
        case 'd': return "double"; break;
        case 's': return "string"; break;
        case 'b': return "blob"; break;
        case 't': return "time tag"; break;
        default: return "unknown type";
      }
    }
    
    static const char* argTypeToString(std::string&)
    {
      return "string";
    }
    
    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value, const char*>
    argTypeToString(T&)
    {
      return "integer";
    }

    template <typename T>
    static std::enable_if_t<std::is_floating_point<T>::value, const char*>
    argTypeToString(T&)
    {
      return "float";
    }

    static const char* argTypeToString(BufferT::type&)
    {
      return "buffer";
    }
    
    static const char* argTypeToString(InputBufferT::type&)
    {
      return "buffer";
    }
    
    template <typename P>
    static std::enable_if_t<IsSharedClient<P>::value,const char*>
    argTypeToString(P&)
    {
      return "shared_object"; //not ideal
    }

    static bool argTypeOK(std::string&, char tag)
    {
      return tag == 's';
    }
    
    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value
                            || std::is_floating_point<T>::value, bool>
    argTypeOK(T&, char tag)
    {
      return tag == 'i' || tag == 'f' || tag == 'd';
    }

    static bool argTypeOK(BufferT::type&, char tag)
    {
      return tag == 'i';
    }
    
    static bool argTypeOK(InputBufferT::type&, char tag)
    {
      return tag == 'i';
    }
    
    template <typename P>
    static std::enable_if_t<IsSharedClient<P>::value,bool>
    argTypeOK(P&, char tag)
    {
      return tag == 's';
    }
    
    static auto fromArgs(Unit*, sc_msg_iter* args, std::string, int)
    {
      const char* recv = args->gets("");

      return std::string(recv ? recv : "");
    }

    static auto fromArgs(Unit* x, FloatControlsIter& args, std::string, int)
    {
      // first is string size, then chars
      index size = static_cast<index>(args.next());
      
      auto ft = FluidSCWrapper::getInterfaceTable();
      auto w = x->mWorld;
      char* chunk =
          static_cast<char*>(ft->fRTAlloc(w, asUnsigned(size + 1)));

      if (!chunk)
      {
        std::cout << "ERROR: " << FluidSCWrapper::getName()
                  << ": RT memory allocation failed\n";
        return std::string{""};
      }

      for (index i = 0; i < size; ++i)
        chunk[i] = static_cast<char>(args.next());

      chunk[size] = 0; // terminate string
      auto res =  std::string{chunk};
      ft->fRTFree(w,chunk);
      return res;
    }

    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value, T>
    fromArgs(Unit*, FloatControlsIter& args, T, int)
    {
      return static_cast<index>(args.next());
    }

    template <typename T>
    static std::enable_if_t<std::is_floating_point<T>::value, T>
    fromArgs(Unit*, FloatControlsIter& args, T, int)
    {
      return args.next();
    }

    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value, T>
    fromArgs(Unit*, sc_msg_iter* args, T, int defVal)
    {
      return args->geti(defVal);
    }

    template <typename T>
    static std::enable_if_t<std::is_floating_point<T>::value, T>
    fromArgs(Unit*, sc_msg_iter* args, T, int)
    {
      return args->getf();
    }

    static SCBufferAdaptor* fetchBuffer(Unit* x, index bufnum)
    {
      if(bufnum >= x->mWorld->mNumSndBufs)
      {
        index localBufNum = bufnum - x->mWorld->mNumSndBufs;
        
        Graph* parent = x->mParent;
        
        return localBufNum <= parent->localMaxBufNum ?
                                  new SCBufferAdaptor(parent->mLocalSndBufs + localBufNum,x->mWorld,true)
                                  : nullptr;
      }
      else
        return bufnum >= 0 ? new SCBufferAdaptor(bufnum, x->mWorld) : nullptr;
    }

    static auto fromArgs(Unit* x, ArgType args, BufferT::type&, int)
    {
      typename LongT::type bufnum = static_cast<typename LongT::type>(
          ParamReader::fromArgs(x, args, typename LongT::type(), -1));
      return BufferT::type(fetchBuffer(x, bufnum));
    }

    static auto fromArgs(Unit* x, ArgType args, InputBufferT::type&, int)
    {
      typename LongT::type bufnum =
          static_cast<LongT::type>(fromArgs(x, args, LongT::type(), -1));
      return InputBufferT::type(fetchBuffer(x, bufnum));
    }

    template <typename P>
    static std::enable_if_t<IsSharedClient<P>::value, P>
    fromArgs(Unit* x, ArgType args, P&, int)
    {
      return {fromArgs(x, args, std::string{}, 0).c_str()};
    }
  };


  // Iterate over arguments via callbacks from params object
  template <typename ArgType, size_t N, typename T>
  struct Setter
  {
    static constexpr index argSize =
        C::getParameterDescriptors().template get<N>().fixedSize;

    typename T::type operator()(Unit* x, ArgType args)
    {
      // Just return default if there's nothing left to grab
      if (args.remain() == 0)
      {
        std::cout << "WARNING: " << getName()
                  << " received fewer parameters than expected\n";
        return C::getParameterDescriptors().template makeValue<N>();
      }

      ParamLiteralConvertor<T, argSize> a;
      using LiteralType =
          typename ParamLiteralConvertor<T, argSize>::LiteralType;

      for (index i = 0; i < argSize; i++)
        a[i] = static_cast<LiteralType>(
            ParamReader<ArgType>::fromArgs(x, args, a[0], 0));

      return a.value();
    }
  };

  template <size_t N, typename T>
  using ArgumentSetter = Setter<sc_msg_iter*, N, T>;

  template <size_t N, typename T>
  using ControlSetter = Setter<FloatControlsIter&, N, T>;

  // CryingEmoji.png: SC API hides all the useful functions for sending
  // replies back to the language with things like, uh, strings and stuff.
  // We have Node_SendReply, which assumes you are sending an array of float,
  // and must be called only from the RT thread. Thanks.
  // So, we do in reverse what the SendReply Ugen does, and parse
  // an array of floats as characters in the language. VomitEmoji.png

  struct ToFloatArray
  {
    static index allocSize(typename BufferT::type) { return 1; }

    template <typename T>
    static std::enable_if_t<
        std::is_integral<T>::value || std::is_floating_point<T>::value, index>
        allocSize(T)
    {
      return 1;
    }

    static index allocSize(std::string s)
    {
      return asSigned(s.size()) + 1;
    } // put null char at end when we send

    static index allocSize(FluidTensor<std::string, 1> s)
    {
      index count = 0;
      for (auto& str : s) count += (str.size() + 1);
      return count;
    }

    template <typename T>
    static index allocSize(FluidTensor<T, 1> s)
    {
      return s.size();
    }

    template <typename... Ts>
    static std::tuple<std::array<index, sizeof...(Ts)>, index>
    allocSize(std::tuple<Ts...>&& t)
    {
      return allocSizeImpl(std::forward<decltype(t)>(t),
                           std::index_sequence_for<Ts...>());
    };

    template <typename... Ts, size_t... Is>
    static std::tuple<std::array<index, sizeof...(Ts)>, index>
    allocSizeImpl(std::tuple<Ts...>&& t, std::index_sequence<Is...>)
    {
      index                            size{0};
      std::array<index, sizeof...(Ts)> res;
      (void) std::initializer_list<int>{
          (res[Is] = size, size += ToFloatArray::allocSize(std::get<Is>(t)),
           0)...};
      return std::make_tuple(res,
                             size); // array of offsets into allocated buffer &
                                    // total number of floats to alloc
    };

    static void convert(float* f, typename BufferT::type buf)
    {
      f[0] = static_cast<SCBufferAdaptor*>(buf.get())->bufnum();
    }

    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value ||
                            std::is_floating_point<T>::value>
    convert(float* f, T x)
    {
      f[0] = static_cast<float>(x);
    }

    static void convert(float* f, std::string s)
    {
      std::copy(s.begin(), s.end(), f);
      f[s.size()] = 0; // terminate
    }
    static void convert(float* f, FluidTensor<std::string, 1> s)
    {
      for (auto& str : s)
      {
        std::copy(str.begin(), str.end(), f);
        f += str.size();
        *f++ = 0;
      }
    }
    template <typename T>
    static void convert(float* f, FluidTensor<T, 1> s)
    {
      static_assert(std::is_convertible<T, float>::value,
                    "Can't convert this to float output");
      std::copy(s.begin(), s.end(), f);
    }

    template <typename... Ts, size_t... Is>
    static void convert(float* f, std::tuple<Ts...>&& t,
                        std::array<index, sizeof...(Ts)> offsets,
                        std::index_sequence<Is...>)
    {
      (void) std::initializer_list<int>{
          (convert(f + offsets[Is], std::get<Is>(t)), 0)...};
    }
  };


  // So, to handle a message to a plugin we will need to
  // (1) Launch the invovation of the message on the SC NRT Queue using FIFO
  // Message (2) Run the actual function (maybe asynchronously, in our own
  // thread) (3) Launch an asynchronous command to send the reply back (in Stage
  // 3)

  template <size_t N, typename Ret, typename ArgTuple>
  struct MessageDispatch
  {
    static constexpr size_t Message = N;
    SharedState             state;
    ArgTuple                args;
    Ret                     result;
    std::string             name;
  };

  // Sets up a single /u_cmd
  template <size_t N, typename T>
  struct SetupMessage
  {
    void operator()(const T& message)
    {
      auto ft = getInterfaceTable();
      ft->fDefineUnitCmd(getName(), message.name, launchMessage<N>);
    }
  };

  template <size_t N>
  static void launchMessage(Unit* u, sc_msg_iter* args)
  {
    FluidSCWrapper* x = static_cast<FluidSCWrapper*>(u);
    using IndexList =
        typename Client::MessageSetType::template MessageDescriptorAt<
            N>::IndexList;
    launchMessageImpl<N>(x, args, IndexList());
  }

  template <size_t N, size_t... Is>
  static void launchMessageImpl(FluidSCWrapper* x, sc_msg_iter* inArgs,
                                std::index_sequence<Is...>)
  {
    using MessageDescriptor =
        typename Client::MessageSetType::template MessageDescriptorAt<N>;
    using ArgTuple = typename MessageDescriptor::ArgumentTypes;
    using ReturnType = typename MessageDescriptor::ReturnType;
    using IndexList = typename MessageDescriptor::IndexList;
    using MessageData = MessageDispatch<N, ReturnType, ArgTuple>;
    auto         ft = getInterfaceTable();
    void*        msgptr = ft->fRTAlloc(x->mWorld, sizeof(MessageData));
    MessageData* msg = new (msgptr) MessageData;
    msg->name = '/' + Client::getMessageDescriptors().template name<N>();
    msg->state = x->state();
    ArgTuple&    args = msg->args;
    // type check OSC message
    
    std::string tags(inArgs->tags + inArgs->count);//evidently this needs commenting: construct string at pointer offset by tag count, to pick up args
    bool willContinue = true;
    bool typesMatch = true;
    
    constexpr size_t expectedArgCount = std::tuple_size<ArgTuple>::value;
    
    if(tags.size() > expectedArgCount)
    {
      std::cout << "WARNING: " << msg->name << " received more arguments than expected (got "
                << tags.size() << ", expect " << expectedArgCount << ")\n";
    }
    
    if(tags.size() < expectedArgCount)
    {
      std::cout << "ERROR: " << msg->name << " received fewer arguments than expected (got "
                << tags.size() << ", expect " << expectedArgCount << ")\n";
      willContinue = false;
    }

    auto tagsIter = tags.begin();
    auto tagsEnd  = tags.end();
    ForEach(args,[&typesMatch,&tagsIter,&tagsEnd](auto& arg){
       if(tagsIter == tagsEnd)
       {
          typesMatch = false;
          return;
       }
       char t = *(tagsIter++);
       typesMatch = typesMatch && ParamReader<sc_msg_iter*>::argTypeOK(arg,t);
    });
    
   willContinue = willContinue && typesMatch;
   
   if(!typesMatch)
   {
      auto& report = std::cout;
      report << "ERROR: " << msg->name << " type signature incorrect.\nExpect: (";
      size_t i{0};
      ForEach(args, [&i](auto& x){
        report << ParamReader<sc_msg_iter*>::argTypeToString(x);
        if(i < ( expectedArgCount - 1 ) )
        {
          report << " ,";
        }
        i++;
      });
      report << ")\nReceived: (";
      i = 0;
      for(auto t: tags)
      {
        report << ParamReader<sc_msg_iter*>::oscTagToString(t);
        if( i < ( tags.size() - 1 ) )
        {
          report << ", ";
        }
        i++;
      }
      report << ")\n";
   }
   
    if(!willContinue)
    {
        msg->~MessageData();
        ft->fRTFree(x->mWorld, msgptr);
        return;
    }
    ForEach(args,[x,&inArgs](auto& arg){
      arg = ParamReader<sc_msg_iter*>::fromArgs(x, inArgs,arg,0);
    });

    ft->fDoAsynchronousCommand(
        x->mWorld, nullptr, getName(), msg,
        [](World*, void* data) // NRT thread: invocation
        {
          MessageData* m = static_cast<MessageData*>(data);
          m->result =
              ReturnType{invokeImpl<N>(m->state, m->args, IndexList{})};

          if (!m->result.ok())
            printResult(m->state, m->result);

          return true;
        },
        [](World* world, void* data) // RT thread:  response
        {
          MessageData* m = static_cast<MessageData*>(data);
          MessageDescriptor::template forEachArg<typename BufferT::type,
                                                 impl::AssignBuffer>(m->args,
                                                                     world);
         
          if(m->result.status() != Result::Status::kError)
            messageOutput(m->state, m->name, m->result);
          else
          {
             auto ft = getInterfaceTable();
            if(m->state->mNodeAlive)
              ft->fSendNodeReply(m->state->mNode,
                        -1, m->name.c_str(),0, nullptr);
          }
          return true;
        },
        nullptr,                 // NRT Thread: No-op
        [](World* w, void* data) // RT thread: clean up
        {
          MessageData* m = static_cast<MessageData*>(data);
          m->~MessageData();
          getInterfaceTable()->fRTFree(w, data);
        },
        0, nullptr);
  }

  template <size_t N, typename ArgsTuple, size_t... Is> // Call from NRT
  static decltype(auto) invokeImpl(SharedState& x, ArgsTuple& args,
                                   std::index_sequence<Is...>)
  {
    return x->client.template invoke<N>(x->client, std::get<Is>(args)...);
  }
  
  template <typename T> // call from RT
  static void messageOutput(SharedState& x, const std::string& s,
                            MessageResult<T>& result)
  {
    auto ft = getInterfaceTable();
    if(!x->mNodeAlive) return;
    
    // allocate return values
    index  numArgs = ToFloatArray::allocSize(static_cast<T>(result));

    if(numArgs > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numArgs) * sizeof(float) << " bytes)." << std::endl;
      return;
    }
    
    float* values = static_cast<float*>(
        ft->fRTAlloc(x->mNode->mWorld, asUnsigned(numArgs) * sizeof(float)));
    
    // copy return data
    ToFloatArray::convert(values, static_cast<T>(result));
    
    ft->fSendNodeReply(x->mNode, -1, s.c_str(),
                       static_cast<int>(numArgs), values);
  }

  static void messageOutput(SharedState& x, const std::string& s,
                            MessageResult<void>&)
  {
    auto ft = getInterfaceTable();
    if(!x->mNodeAlive) return;
    ft->fSendNodeReply(x->mNode, -1, s.c_str(), 0, nullptr);
  }

  template <typename... Ts>
  static void messageOutput(SharedState& x, const std::string& s,
                            MessageResult<std::tuple<Ts...>>& result)
  {
    auto                             ft = getInterfaceTable();
    std::array<index, sizeof...(Ts)> offsets;
    index                            numArgs;
    if(!x->mNodeAlive) return;
    
    std::tie(offsets, numArgs) =
        ToFloatArray::allocSize(static_cast<std::tuple<Ts...>>(result));
    
    if(numArgs > 2048)
    {
      std::cout << "ERROR: Message response too big to send (" << asUnsigned(numArgs) * sizeof(float) << " bytes)." << std::endl;
      return;
    }
    
    float* values = static_cast<float*>(
        ft->fRTAlloc(x->mNode->mWorld, asUnsigned(numArgs) * sizeof(float)));
    
    ToFloatArray::convert(values, std::tuple<Ts...>(result), offsets,
                          std::index_sequence_for<Ts...>());
    
    ft->fSendNodeReply(x->mNode, -1, s.c_str(),
                       static_cast<int>(numArgs), values);
  }


  static void doVersion(Unit*, sc_msg_iter*)
  {
    std::cout << "Fluid Corpus Manipualtion Toolkit version " << fluidVersion()
              << std::endl;
  }


public:
  using Client = C;
  using ParamSetType = typename C::ParamSetType;

  FluidSCWrapper(FloatControlsIter&& i, Client&& c, ParamSetType&& p):
          mControlsIterator{std::move(i)},
  mState{new WrapperState<Client>{std::move(p),std::move(c),&SCUnit::mParent->mNode}}
  {
    client().setParams(params()); //<-IMPORTANT: client's ref to params is by address, and this has just changed
    impl::FluidSCWrapperBase<Client>::init();
  }

  ~FluidSCWrapper()
  {
    mState->mNodeAlive = false;
  }


  std::shared_ptr<WrapperState<Client>>& state() { return mState; }


  static const char* getName(const char* setName = nullptr)
  {
    static const char* name = nullptr;
    return (name = setName ? setName : name);
  }

  static InterfaceTable* getInterfaceTable(InterfaceTable* setTable = nullptr)
  {
    static InterfaceTable* ft = nullptr;
    return (ft = setTable ? setTable : ft);
  }

  static void setup(InterfaceTable* ft, const char* name)
  {
    getName(name);
    getInterfaceTable(ft);
    registerUnit(ft, name);
    impl::FluidSCWrapperBase<Client>::setup(ft, name);
    Client::getMessageDescriptors().template iterate<SetupMessage>();
    ft->fDefineUnitCmd(name, "version", doVersion);
  }

  static auto& setParams(Unit* x, ParamSetType& p,
                         FloatControlsIter& inputs, bool constrain = false)
  {
      //TODO: Regain this robustness if possible? 
    // We won't even try and set params if the arguments don't match
    // if (inputs.size() == C::getParameterDescriptors().count())
    // {
      p.template setParameterValues<ControlSetter>(x->mWorld->mVerbosity > 0, x, inputs);
      if (constrain) p.constrainParameterValues();
    // }
    // else
    // {
    //   std::cout << "ERROR: " << getName()
    //             << ": parameter count mismatch. Perhaps your binary plugins "
    //                "and SC sources are different versions"
    //             << std::endl;
    // }

    return p;
  }

  static void printResult(SharedState& x, Result& r)
  {
    if (!x.get() || !x->mNodeAlive) return;

    switch (r.status())
    {
    case Result::Status::kWarning: {
      if (x->mNode->mWorld->mVerbosity > 0)
        std::cout << "WARNING: " << r.message().c_str() << '\n';
      break;
    }
    case Result::Status::kError: {
      std::cout << "ERROR: " << r.message().c_str() << '\n';
      break;
    }
    case Result::Status::kCancelled: {
      std::cout << "Task cancelled\n" << '\n';
      break;
    }
    default: {
    }
    }
  }
  
  auto& client() { return mState->client; }
  auto& params() { return mState->params; }
  
private:
  
  static void registerUnit(InterfaceTable* ft, const char* name) {
    UnitCtorFunc ctor =impl::FluidSCWrapperBase<Client>::constructClass;
    UnitDtorFunc dtor = impl::FluidSCWrapperBase<Client>::destroyClass;
    (*ft->fDefineUnit)(name, sizeof(FluidSCWrapper), ctor, dtor, 0);
  }
  
  FloatControlsIter mControlsIterator;
  std::shared_ptr<WrapperState<Client>> mState;
};

template <typename Client>
void makeSCWrapper(const char* name, InterfaceTable* ft)
{
  FluidSCWrapper<Client>::setup(ft, name);
}

} // namespace client
} // namespace fluid
