#pragma once

#include "BufferFuncs.hpp"
#include "CopyReplyAddress.hpp"
#include "Messaging.hpp"
#include "Meta.hpp"
#include "RealTimeBase.hpp"
#include "SCBufferAdaptor.hpp"
#include "SCWorldAllocator.hpp"
#include <clients/common/FluidBaseClient.hpp>
#include <data/FluidMeta.hpp>
#include <SC_PlugIn.hpp>
#include <mutex>
#include <scsynthsend.h>
#include <unordered_map>

namespace fluid {
namespace client {
namespace impl {

/// Non Real Time Processor

  using ServerCommandFn = void(*)(World* world, void*, struct sc_msg_iter* args,
                            void* replyAddr);
 
  using CommandMap =  std::unordered_map<std::string_view, ServerCommandFn>;


template <typename Client, typename Wrapper>
class NonRealTime : public SCUnit
{
public:
  using Params = typename Client::ParamSetType;
private:
  template <typename T, typename... Args>
  static T* rtalloc(World* world, Args&&... args)
  {
    void* space = getInterfaceTable()->fRTAlloc(world, sizeof(T));
    return new (space) T{std::forward<Args>(args)...};
  }

  /// Instance cache
  struct CacheEntry
  {
    CacheEntry(const Params& p) : mParams{p}, mClient{mParams} {}

    Params            mParams;
    Client            mClient;
    std::atomic<bool> mDone{false};
  };

  using CacheEntryPointer = std::shared_ptr<CacheEntry>;
  using WeakCacheEntryPointer =
      std::weak_ptr<CacheEntry>; // could use weak_type in 17

public:
  using Cache = std::unordered_map<index, CacheEntryPointer>;
  using RTCacheAllocator =
      SCWorldAllocator<std::pair<const index, WeakCacheEntryPointer>, Wrapper>;

  struct RTCacheMirror
      : public std::unordered_map<index, WeakCacheEntryPointer,
                                  std::hash<index>, std::equal_to<index>,
                                  RTCacheAllocator>
  {

    RTCacheMirror(RTCacheAllocator&& alloc)
        : std::unordered_map<index, WeakCacheEntryPointer, std::hash<index>,
                             std::equal_to<index>, RTCacheAllocator>{
              std::move(alloc)}
    {
      // std::cout << "Warning: And up...\n" << std::endl;
    }

    ~RTCacheMirror()
    {
      // std::cout << "Warning: And down...\n" << std::endl;
    }
  };

  static Cache mCache;

  static RTCacheMirror& rtCache(World* world)
  {
    thread_local static RTCacheMirror mRTCache(
        RTCacheAllocator(world, Wrapper::getInterfaceTable()));
    return mRTCache;
  }

private:
  static bool isNull(WeakCacheEntryPointer const& weak)
  {
    return !weak.owner_before(WeakCacheEntryPointer{}) &&
           !WeakCacheEntryPointer{}.owner_before(weak);
  }

  static WeakCacheEntryPointer rtget(World* world, index id)
  {
    auto lookup = rtCache(world).find(id);
    return lookup == rtCache(world).end() ? WeakCacheEntryPointer()
                                          : lookup->second;
  }

  using RawCacheEntry = typename Cache::value_type;

  struct addToRTCache
  {
    void operator()(World* w, RawCacheEntry& r)
    {
      FifoMsg msg;
      auto    add = [](FifoMsg* m) {
        RawCacheEntry* r = static_cast<RawCacheEntry*>(m->mData);
        rtCache(m->mWorld).emplace(r->first, r->second);
      };
      msg.Set(w, add, nullptr, &r);
      auto ft = Wrapper::getInterfaceTable();
      if (!ft->fSendMsgToRT(w, msg))
      {
        std::cout << "ERROR: Message to RT failed";
      }
    }
  };

  struct removeFromRTCache
  {
    void operator()(World* world, index id)
    {
      index* data = new index();
      *data = id;

      FifoMsg msg;

      auto remove = [](FifoMsg* m) {
        int* id = static_cast<int*>(m->mData);
        rtCache(m->mWorld).erase(*id);
      };

      auto cleanup = [](FifoMsg* m) { delete static_cast<index*>(m->mData); };

      msg.Set(world, remove, cleanup, data);
      auto ft = Wrapper::getInterfaceTable();
      ft->fSendMsgToRT(world, msg);
    }
  };


public:
  static WeakCacheEntryPointer get(index id)
  {
    auto lookup = mCache.find(id);
    return lookup == mCache.end() ? WeakCacheEntryPointer() : lookup->second;
  }

  static WeakCacheEntryPointer add(World* world, index id, const Params& params)
  {
    if (isNull(get(id)))
    {
      auto result = mCache.emplace(id, std::make_shared<CacheEntry>(params));

      addToRTCache{}(world, *(result.first));

      return result.second ? (result.first)->second
                           : WeakCacheEntryPointer(); // sob
    }
    else // client has screwed up
    {
      std::cout << "ERROR: " << Wrapper::getName() << " ID " << id
                << " already in use\n";
      return {};
    }
  }

  static void remove(World* world, index id)
  {
    mCache.erase(id);
    removeFromRTCache{}(world, id);
  }

  static void printNotFound(index id)
  {
    std::cout << "ERROR: " << Wrapper::getName() << " no instance with ID "
              << id << std::endl;
  }

private:
  static InterfaceTable* getInterfaceTable()
  {
    return Wrapper::getInterfaceTable();
  }

  template <size_t N, typename T>
  using ParamsFromOSC =
      typename ClientParams<Wrapper>::template Setter<sc_msg_iter, N, T>;

  template <size_t N, typename T>
  using ParamsFromSynth =
      typename ClientParams<Wrapper>::template Setter<impl::FloatControlsIter,
                                                      N, T>;

  struct NRTCommand
  {
    NRTCommand(World*, sc_msg_iter* args, void* replyAddr,
               bool consumeID = true)
    {
      auto count = args->count;
      auto pos = args->rdpos;

      mID = args->geti();

      if (!consumeID)
      {
        args->count = count;
        args->rdpos = pos;
      }

      if (replyAddr) mReplyAddress = copyReplyAddress(replyAddr);
    }

    ~NRTCommand()
    {
      if (mReplyAddress) deleteReplyAddress(mReplyAddress);
    }

    NRTCommand() {}

    explicit NRTCommand(index id) : mID{id} {}

    bool stage2(World*) { return true; }  // nrt
    bool stage3(World*) { return true; }  // rt
    bool stage4(World*) { return false; } // nrt
    void cleanup(World*) {}               // rt

    void sendReply(const char* name, bool success)
    {
      if (mReplyAddress)
      {
        std::string    slash{"/"};
        small_scpacket packet;
        packet.adds((slash + name).c_str());
        packet.maketags(3);
        packet.addtag(',');
        packet.addtag('i');
        packet.addtag('i');
        packet.addi(success);
        packet.addi(static_cast<int>(mID));

        SendReply(mReplyAddress, packet.data(),
                  static_cast<int>(packet.size()));
      }
    }
    //      protected:
    index mID;
    void* mReplyAddress{nullptr};
  };

  struct CommandNew : public NRTCommand
  {
    CommandNew(World* world, sc_msg_iter* args, void* replyAddr)
        : NRTCommand{world, args, replyAddr, !IsNamedShared_v<Client>},
          mParams{Client::getParameterDescriptors()}
    {
      mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr, world,
                                                           *args);
    }

    CommandNew(index id, World*, FloatControlsIter& args, Unit* x)
        : NRTCommand{id}, mParams{Client::getParameterDescriptors()}
    {
      mParams.template setParameterValuesRT<ParamsFromSynth>(nullptr, x, args);
    }

    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/new";
      return cmd.c_str();
    }

    bool stage2(World* w)
    {
      Result constraintsRes = validateParameters(mParams);

      if (!constraintsRes.ok()) Wrapper::printResult(w, constraintsRes);

      mResult = (!isNull(add(w, NRTCommand::mID, mParams)));

      // Sigh. The cache entry above has both the client instance and main
      // params instance.
      // The client is linked to the params by reference; I've not got the
      // in-place constrction working properly so that params are in their final
      // resting place by the time we make the client so (for) now we need to
      // manually repoint the client to the correct place. Or badness.
      if (mResult)
      {
        auto ptr = get(NRTCommand::mID).lock();
        ptr->mClient.setParams(ptr->mParams);
      }

      NRTCommand::sendReply(name(), mResult);

      return mResult;
    }

  private:
    bool   mResult;
    Params mParams;
  };

  struct CommandFree : public NRTCommand
  {
    using NRTCommand::NRTCommand;

    void cancelCheck(std::false_type, index id)
    {
      if (auto ptr = get(id).lock())
      {
        auto& client = ptr->mClient;
        if (!client.synchronous() &&
            client.state() == ProcessState::kProcessing)
          std::cout << Wrapper::getName() << ": Processing cancelled"
                    << std::endl;
      }
    }

    void cancelCheck(std::true_type, index) {}


    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/free";
      return cmd.c_str();
    }

    bool stage2(World* world)
    {
      cancelCheck(IsRTQueryModel_t(), NRTCommand::mID);
      remove(world, NRTCommand::mID);
      NRTCommand::sendReply(name(), true);
      return true;
    }
  };

  struct CommandProcess : public NRTCommand
  {
    CommandProcess(World* world, sc_msg_iter* args, void* replyAddr)
        : NRTCommand{world, args, replyAddr},
          mParams{Client::getParameterDescriptors()}
    {
      auto& ar = *args;
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        ptr->mDone.store(false, std::memory_order_release);
        mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr, world,
                                                             ar);
        mSynchronous = static_cast<bool>(ar.geti());
      } // if this fails, we'll hear about it in stage2 anyway
    }

    explicit CommandProcess(index id, bool synchronous, Params* params)
        : NRTCommand{id},
          mSynchronous(synchronous), mParams{Client::getParameterDescriptors()}
    {
      if (params)
      {
        mParams = *params;
        mOverwriteParams = true;
      }
    }

    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/process";
      return cmd.c_str();
    }

    bool stage2(World* world)
    {
      mRecord = get(NRTCommand::mID);
      if (auto ptr = mRecord.lock())
      {

        auto& params = ptr->mParams;
        if (mOverwriteParams) params = mParams;
        auto& client = ptr->mClient;

        Result result = validateParameters(params);
        Wrapper::printResult(world, result);
        if (result.status() != Result::Status::kError)
        {
          client.setSynchronous(mSynchronous);
          index  id = NRTCommand::mID;
          size_t completionMsgSize = mCompletionMsgSize;
          char*  completionMessage = mCompletionMessage;
          void*  replyAddress = copyReplyAddress(NRTCommand::mReplyAddress);
          auto   callback = [world, id, completionMsgSize, completionMessage,
                           replyAddress]() {
            doProcessCallback(world, id, completionMsgSize, completionMessage,
                              replyAddress);
          };

          result = mSynchronous ? client.enqueue(params)
                                : client.enqueue(params, callback);
          Wrapper::printResult(world, result);

          if (result.status() != Result::Status::kError)
          {
            ptr->mDone.store(false, std::memory_order_release);
            mResult = client.process();
            Wrapper::printResult(world, mResult);

            bool error = mResult.status() == Result::Status::kError;

            if (error) ptr->mDone.store(true, std::memory_order_release);
            bool toStage3 = mSynchronous && !error;
            return toStage3;
          }
        }
      }
      else
      {
        mResult = Result{Result::Status::kError, "No ", Wrapper::getName(),
                         " with ID ", NRTCommand::mID};
        Wrapper::printResult(world, mResult);
      }
      return false;
    }

    // Only for blocking execution
    bool stage3(World* world) // rt
    {
      if (auto ptr = mRecord.lock())
      {
        ptr->mParams.template forEachParamType<BufferT, AssignBuffer>(world);
        return true;
      }
      return false;
    }

    // Only for blocking execution
    bool stage4(World*) // nrt
    {
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        ptr->mParams.template forEachParamType<BufferT, impl::CleanUpBuffer>();

        if (NRTCommand::mID >= 0 && mSynchronous)
          NRTCommand::sendReply(name(),
                                mResult.status() != Result::Status::kError);
        ptr->mDone.store(true, std::memory_order_release);
        return true;
      }
      return false;
    }

    bool synchronous() { return mSynchronous; }

    void addCompletionMessage(size_t size, char* message) //, void* addr)
    {
      mCompletionMsgSize = size;
      mCompletionMessage = message;
    }

    // private:
    Result                mResult;
    bool                  mSynchronous;
    size_t                mCompletionMsgSize{0};
    char*                 mCompletionMessage{nullptr};
    Params                mParams;
    bool                  mOverwriteParams{false};
    WeakCacheEntryPointer mRecord;
  };


  /// Not registered as a PlugInCmd. Triggered by  worker thread callback
  struct CommandAsyncComplete : public NRTCommand
  {
    CommandAsyncComplete(World*, index id, void* replyAddress)
    {
      NRTCommand::mID = id;
      NRTCommand::mReplyAddress = replyAddress;
    }

    static const char* name() { return CommandProcess::name(); }

    bool stage2(World* world)
    {
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        Result r;
        mRecord = ptr;
        auto&        client = ptr->mClient;
        ProcessState s = client.checkProgress(r);
        if (s == ProcessState::kDone || s == ProcessState::kDoneStillProcessing)
        {
          if (r.status() == Result::Status::kCancelled)
          {
            std::cout << Wrapper::getName() << ": Processing cancelled"
                      << std::endl;
            ptr->mDone.store(true, std::memory_order_release);
            return false;
          }

          client.checkProgress(r);
          mSuccess = !(r.status() == Result::Status::kError);
          Wrapper::printResult(world, r);
          if (!mSuccess)
          {
            ptr->mDone.store(true, std::memory_order_release);
            return false;
          }
          // if we're progressing to stage3, don't unlock the lock just yet
          //          lock.release();
          return true;
        }
      }
      return false;
    }

    bool stage3(World* world)
    {
      if (auto ptr = mRecord.lock())
      {
        auto& params = ptr->mParams;
        params.template forEachParamType<BufferT, AssignBuffer>(world);
      }
      return true;
    }

    bool stage4(World*) // nrt
    {
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        ptr->mParams.template forEachParamType<BufferT, impl::CleanUpBuffer>();

        if (NRTCommand::mID >= 0 && NRTCommand::mReplyAddress)
        {
          NRTCommand::sendReply(name(), mSuccess);
        }

        ptr->mDone.store(true, std::memory_order_release); // = true;
        return true;
      }
      std::cout << "ERROR: Failed to lock\n";
      return false;
    }

    bool                  mSuccess;
    WeakCacheEntryPointer mRecord;
  };


  static void doProcessCallback(World* world, index id,
                                size_t completionMsgSize,
                                char* completionMessage, void* replyAddress)
  {
    auto ft = getInterfaceTable();
    struct Context
    {
      World* mWorld;
      index  mID;
      size_t mCompletionMsgSize;
      char*  mCompletionMessage;
      void*  mReplyAddress;
    };

    Context* c = new Context{world, id, completionMsgSize, completionMessage,
                             replyAddress};

    auto launchCompletionFromNRT = [](FifoMsg* inmsg) {
      auto runCompletion = [](FifoMsg* msg) {
        Context* c = static_cast<Context*>(msg->mData);
        World*   world = c->mWorld;
        index    id = c->mID;
        auto     ft = getInterfaceTable();
        void*    space = ft->fRTAlloc(world, sizeof(CommandAsyncComplete));
        CommandAsyncComplete* cmd =
            new (space) CommandAsyncComplete(world, id, c->mReplyAddress);
        if (runAsyncCommand(world, cmd, c->mReplyAddress, c->mCompletionMsgSize,
                            c->mCompletionMessage) != 0)
        {
          std::cout << "ERROR: Async cmd failed in callback" << std::endl;
        }
        if (c->mCompletionMsgSize) ft->fRTFree(world, c->mCompletionMessage);
      };

      auto tidyup = [](FifoMsg* msg) {
        Context* c = static_cast<Context*>(msg->mData);
        delete c;
      };

      auto    ft = getInterfaceTable();
      FifoMsg fwd = *inmsg;
      fwd.Set(inmsg->mWorld, runCompletion, tidyup, inmsg->mData);
      if (inmsg->mWorld->mRunning)
        if (!ft->fSendMsgToRT(inmsg->mWorld, fwd))
        {
          std::cout << "ERROR: Failed to queue -> RT\n";
        }
    };

    FifoMsg msg;
    msg.Set(world, launchCompletionFromNRT, nullptr, c);

    if (world->mRunning)
    {
      ft->fNRTLock(world);
      msg.Perform();
      ft->fNRTUnlock(world);
    }
  }


  struct CommandProcessNew : public NRTCommand
  {
    CommandProcessNew(World* world, sc_msg_iter* args, void* replyAddr)
        : mNew{world, args, replyAddr}, mProcess{mNew.mID, false, nullptr}
    {
      mProcess.mSynchronous = args->geti();
      mProcess.mReplyAddress = mNew.mReplyAddress;
    }

    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/processNew";
      return cmd.c_str();
    }

    bool stage2(World* world)
    {
      return mNew.stage2(world) ? mProcess.stage2(world) : false;
    }

    bool stage3(World* world) // rt
    {
      return mProcess.stage3(world);
    }

    bool stage4(World* world) // nrt
    {
      return mProcess.stage4(world);
    }

    void cleanup(World* world)
    {
      mProcess.mReplyAddress = nullptr;
      mProcess.cleanup(world);
    }

    bool synchronous() { return mProcess.synchronous(); }

    void addCompletionMessage(size_t size, char* message)
    {
      mProcess.addCompletionMessage(size, message);
    }

  private:
    CommandNew     mNew;
    CommandProcess mProcess;
  };


  struct CommandCancel : public NRTCommand
  {
    CommandCancel(World* world, sc_msg_iter* args, void* replyAddr)
        : NRTCommand{world, args, replyAddr}
    {}

    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/cancel";
      return cmd.c_str();
    }

    bool stage2(World*)
    {
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        auto& client = ptr->mClient;
        if (!client.synchronous())
        {
          client.cancel();
          std::cout << Wrapper::getName() << ": Processing cancelled"
                    << std::endl;
          return true;
        }
      }
      return false;
    }
  };

  struct CommandSetParams : public NRTCommand
  {
    CommandSetParams(World* world, sc_msg_iter* args, void* replyAddr)
        : NRTCommand{world, args, replyAddr}
    {
      auto& ar = *args;
      if (auto ptr = get(NRTCommand::mID).lock())
      {
        ptr->mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr,
                                                                  world, ar);
        Result result = validateParameters(ptr->mParams);
        ptr->mClient.setParams(ptr->mParams);
      }
      else
      {
        mParamsSize = args->size;
        mParamsData = (char*) getInterfaceTable()->fRTAlloc(world, asUnsigned(mParamsSize));
        std::copy_n(args->data, args->size,mParamsData);
//        mArgs = args; GAH WHY ISN"T THIS COPYABLE????
        mArgs.init(mParamsSize,mParamsData);
        mArgs.count = args->count;
        mArgs.rdpos = mParamsData + std::distance(args->data,args->rdpos);
        mTryInNRT = true;
      }
    }

    static const char* name()
    {
      static std::string cmd = std::string(Wrapper::getName()) + "/setParams";
      return cmd.c_str();
    }
    
    bool stage2(World* world)
    {
      
      if(!mTryInNRT) return false;
      
      if (auto ptr = get(NRTCommand::mID).lock())
      {        
        ptr->mParams.template setParameterValues<ParamsFromOSC>(true, world, mArgs);
        Result result = validateParameters(ptr->mParams);
        ptr->mClient.setParams(ptr->mParams);
      }
      else
        printNotFound(NRTCommand::mID);
        
      return true;
    }
    
    bool stage3(World* world)
    {
       if(mParamsData) getInterfaceTable()->fRTFree(world, mParamsData);
       return false;
    }
        
    bool mTryInNRT{false};
    char* mParamsData{nullptr};
    int mParamsSize;
    sc_msg_iter mArgs;
  };


  template <typename Command>
  static auto runAsyncCommand(World* world, Command* cmd, void* replyAddr,
                              size_t completionMsgSize, char* completionMsgData)
  {
    auto ft = getInterfaceTable();

    return ft->fDoAsynchronousCommand(
        world, replyAddr, Command::name(), cmd,
        [](World* w, void* d) { return static_cast<Command*>(d)->stage2(w); },
        [](World* w, void* d) { return static_cast<Command*>(d)->stage3(w); },
        [](World* w, void* d) { return static_cast<Command*>(d)->stage4(w); },
        [](World* w, void* d) {
          auto cmd = static_cast<Command*>(d);
          cmd->cleanup(w);
          cmd->~Command();
          getInterfaceTable()->fRTFree(w, d);
        },
        static_cast<int>(completionMsgSize), completionMsgData);
  }


  static auto runAsyncCommand(World* world, CommandProcess* cmd,
                              void* replyAddr, size_t completionMsgSize,
                              char* completionMsgData)
  {
    if (!cmd->synchronous())
    {

      auto msgcopy =
          (char*) getInterfaceTable()->fRTAlloc(world, completionMsgSize);
      memcpy(msgcopy, completionMsgData, completionMsgSize);
      cmd->addCompletionMessage(completionMsgSize, msgcopy);
      return runAsyncCommand<CommandProcess>(world, cmd, replyAddr, 0, nullptr);
    }
    else
      return runAsyncCommand<CommandProcess>(
          world, cmd, replyAddr, completionMsgSize, completionMsgData);
  }

  static auto runAsyncCommand(World* world, CommandProcessNew* cmd,
                              void* replyAddr, size_t completionMsgSize,
                              char* completionMsgData)
  {
    if (!cmd->synchronous())
    {
      auto msgcopy =
          (char*) getInterfaceTable()->fRTAlloc(world, completionMsgSize);
      memcpy(msgcopy, completionMsgData, completionMsgSize);
      cmd->addCompletionMessage(completionMsgSize, msgcopy);
      return runAsyncCommand<CommandProcessNew>(world, cmd, replyAddr, 0,
                                                nullptr);
    }
    else
      return runAsyncCommand<CommandProcessNew>(
          world, cmd, replyAddr, completionMsgSize, completionMsgData);
  }


  template <typename Command>
  static void defineNRTCommand()
  {
    auto ft = getInterfaceTable();
    auto commandRunner = [](World* world, void*, struct sc_msg_iter* args,
                            void* replyAddr) {
      auto     ft = getInterfaceTable();
      void*    space = ft->fRTAlloc(world, sizeof(Command));
      Command* cmd = new (space) Command(world, args, replyAddr);
      // This is brittle, but can't think of something better offhand
      // This is the only place we can check for a completion message at the end
      // of the OSC packet beause it has to be passed on to DoAsynhronousCommand
      // at this point. However, detecting correctly relies on the Command type
      // having fully consumed arguments from the args iterator in the
      // constructor for cmd
      size_t completionMsgSize{args ? args->getbsize() : 0};
      assert(completionMsgSize <= std::numeric_limits<int>::max());
      char* completionMsgData = nullptr;

      if (completionMsgSize)
      {
        completionMsgData = (char*) ft->fRTAlloc(world, completionMsgSize);
        args->getb(completionMsgData, completionMsgSize);
      }
      runAsyncCommand(world, cmd, replyAddr, completionMsgSize,
                      completionMsgData);

      if (completionMsgSize) ft->fRTFree(world, completionMsgData);
    };    
    mCommandDispatchTable[Command::name()] = commandRunner;
  }


  struct NRTProgressUnit : SCUnit
  {

    static const char* name()
    {
      static std::string n = std::string(Wrapper::getName()) + "Monitor";
      return n.c_str();
    }

    NRTProgressUnit()
    {
      mInterval = static_cast<index>(0.02 / controlDur());
      mID = static_cast<index>(mInBuf[0][0]);
      std::cout << mID << std::endl;
      mRecord = rtget(mWorld, mID);
      set_calc_function<NRTProgressUnit, &NRTProgressUnit::next>();
      Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
    }

    void next(int)
    {

      if (isNull(mRecord)) { mRecord = rtget(mWorld, mID); };

      if (0 == mCounter++)
      {
        if (auto ptr = mRecord.lock())
        {
          mInit = true;
          mDone = ptr->mDone.load(std::memory_order_acquire);
          out0(0) = static_cast<float>(ptr->mClient.progress());
        }
        else
        {
          if (!mInit)
            std::cout << "WARNING: No " << Wrapper::getName() << " with ID "
                      << mID << std::endl;
          else
            mDone = 1;
        }
      }
      mCounter %= mInterval;
    }

  private:
    index                 mID;
    index                 mInterval;
    index                 mCounter{0};
    bool                  mInit{false};
    WeakCacheEntryPointer mRecord;
  };


  struct NRTTriggerUnit : SCUnit
  {

    static index count()
    {
      static index counter = -1;
      return counter--;
    }

    index ControlOffset() { return mSpecialIndex + 1; }

    index ControlSize()
    {
      return index(mNumInputs) - mSpecialIndex // used for oddball cases
             - 3;                              // id + trig + blocking;
    }

    static const char* name()
    {
      static std::string n = std::string(Wrapper::getName()) + "Trigger";
      return n.c_str();
    }

    NRTTriggerUnit()
        : mControlsIterator{mInBuf + ControlOffset(), ControlSize()},
          mParams{Client::getParameterDescriptors()}
    {
      mID = static_cast<index>(mInBuf[0][0]);
      if (mID == -1) mID = count();
      auto cmd = NonRealTime::rtalloc<CommandNew>(mWorld, mID, mWorld,
                                                  mControlsIterator, this);
      if (runAsyncCommand(mWorld, cmd, nullptr, 0, nullptr) != 0)
      {
        std::cout << "ERROR: Async command failed in NRTTriggerUnit()"
                  << std::endl;
      }
      set_calc_function<NRTTriggerUnit, &NRTTriggerUnit::next>();
      Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
    }

    ~NRTTriggerUnit()
    {
      set_calc_function<NRTTriggerUnit, &NRTTriggerUnit::clear>();
      auto cmd = NonRealTime::rtalloc<CommandFree>(mWorld, mID);
      if (runAsyncCommand(mWorld, cmd, nullptr, 0, nullptr) != 0)
      {
        std::cout << "ERROR: Async command failed in ~NRTTriggerUnit()"
                  << std::endl;
      }
    }

    void clear(int)
    {
      Wrapper::getInterfaceTable()->fClearUnitOutputs(
          this, static_cast<int>(mNumOutputs));
    }

    void next(int)
    {
      index triggerInput =
          static_cast<index>(mInBuf[static_cast<index>(mNumInputs) - 2][0]);
      mTrigger = mTrigger || triggerInput;

      bool trigger = (!mPreviousTrigger) && triggerInput; // mTrigger;
      mPreviousTrigger = triggerInput;
      mTrigger = 0;

      if (trigger)
      {
        mControlsIterator.reset(1 + mInBuf); // add one for ID
        Wrapper::setParams(this, mParams, mControlsIterator, true, false);

        bool blocking = mInBuf[mNumInputs - 1][0] > 0;

        CommandProcess* cmd =
            rtalloc<CommandProcess>(mWorld, mID, blocking, &mParams);
        if (runAsyncCommand(mWorld, cmd, nullptr, 0, nullptr) != 0)
        {
          std::cout << "ERROR: Async command failed in NRTTriggerUnit::next()"
                    << std::endl;
        }
        mRunCount++;
      }
      else
      {
        auto record = rtget(mWorld, mID);
        if (auto ptr = record.lock())
        {
          mInit = true;
          auto& client = ptr->mClient;
          mDone = ptr->mDone.load(std::memory_order_acquire);
          out0(0) = mDone ? 1 : static_cast<float>(client.progress());
        }
        else
          mDone = mInit;
      }
    }

  private:
    bool                    mPreviousTrigger{false};
    bool                    mTrigger{false};
    bool                    mBeingFreed{false};
    Result                  mResult;
    impl::FloatControlsIter mControlsIterator;
    index                   mID;
    index                   mRunCount{0};
    WeakCacheEntryPointer   mInst;
    Params                  mParams;
    bool                    mInit{false};
  };

  struct NRTModelQueryUnit : SCUnit
  {
    using Delegate = impl::RealTimeBase<Client, Wrapper>;

    index ControlOffset() { return mSpecialIndex + 2; }
    index ControlSize()
    {
      return index(mNumInputs) - mSpecialIndex // used for oddball cases
             - 2;                              // trig + id
    }

    static const char* name()
    {
      static std::string n = std::string(Wrapper::getName()) + "Query";
      return n.c_str();
    }

    NRTModelQueryUnit()
        // Offset controls by 1 to account for ID
        : mControls{mInBuf + ControlOffset(), ControlSize()}
    {
      mID = static_cast<index>(in0(1));
      init();
      set_calc_function<NRTModelQueryUnit, &NRTModelQueryUnit::next>();
      Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
    }

    void init()
    {
      mInit = false;
      mInst = rtget(mWorld, mID);
      if (auto ptr = mInst.lock())
      {
        auto& client = ptr->mClient;
        mDelegate.init(*this, client, mControls);
        mInit = true;
      }
    }

    void next(int)
    {
      Wrapper::getInterfaceTable()->fClearUnitOutputs(
          this, static_cast<int>(mNumOutputs));
      index id = static_cast<index>(in0(1));
      if (mID != id) init();
      if (!mInit) return;
      if (auto ptr = mInst.lock())
      {
        auto& client = ptr->mClient;
        auto& params = ptr->mParams;
        mControls.reset(mInBuf + ControlOffset());
        mDelegate.next(*this, client, params, mControls, ptr.use_count() == 2);
      }
      else
        printNotFound(id);
    }

  private:
    Delegate              mDelegate;
    FloatControlsIter     mControls;
    index                 mID;
    WeakCacheEntryPointer mInst;
    bool                  mInit{false};
  };


  using ParamSetType = typename Client::ParamSetType;

  template <size_t N, typename T>
  using SetupMessageCmd =
      typename FluidSCMessaging<Wrapper, Client>::template SetupMessageCmd<N,
                                                                           T>;


  template <bool, typename CommandType>
  struct DefineCommandIf
  {
    void operator()() {}
  };


  template <typename CommandType>
  struct DefineCommandIf<true, CommandType>
  {
    void operator()() { defineNRTCommand<CommandType>(); }
  };

  template <bool, typename UnitType>
  struct RegisterUnitIf
  {
    void operator()(InterfaceTable*) {}
  };

  template <typename UnitType>
  struct RegisterUnitIf<true, UnitType>
  {
    void operator()(InterfaceTable* ft)
    {
      registerUnit<UnitType>(ft, UnitType::name());
    }
  };

  using IsRTQueryModel_t = typename Client::isRealTime;
  static constexpr bool IsRTQueryModel = IsRTQueryModel_t::value;

  static constexpr bool IsModel = Client::isModelObject::value;

public:

  static void registerMessage(const char* name, ServerCommandFn f)
  {
     mCommandDispatchTable[name] = f;
  }

  static void setup(InterfaceTable* ft, const char*)
  {
    defineNRTCommand<CommandNew>();
    DefineCommandIf<!IsRTQueryModel, CommandProcess>()();
    DefineCommandIf<!IsRTQueryModel, CommandProcessNew>()();
    DefineCommandIf<!IsRTQueryModel, CommandCancel>()();

    DefineCommandIf<IsModel, CommandSetParams>()();

    defineNRTCommand<CommandFree>();
    RegisterUnitIf<!IsRTQueryModel, NRTProgressUnit>()(ft);
    RegisterUnitIf<!IsRTQueryModel, NRTTriggerUnit>()(ft);

    RegisterUnitIf<IsRTQueryModel, NRTModelQueryUnit>()(ft);
    Client::getMessageDescriptors().template iterate<SetupMessageCmd>();


    static std::string flushCmd = std::string(Wrapper::getName()) + "/flush";

    ft->fDefinePlugInCmd(
        Wrapper::getName(),
        [](World* w, void* inUserData, struct sc_msg_iter* msg, void* replyAddr)
        {
          const char* name = msg->gets();
          
          auto cmd = mCommandDispatchTable.find(name);
          
          if (cmd != mCommandDispatchTable.end())
            cmd->second(w, inUserData ? inUserData : (void*)name, msg, replyAddr);
          else
            std::cout << "ERROR: message " << name << " not registered.";

        }, nullptr);
    
    ft->fDefinePlugInCmd(
        flushCmd.c_str(),
        [](World*, void*, struct sc_msg_iter*, void*) { mCache.clear(); },
        nullptr);
  }


  void init(){};

  static World* getWorld() { return mWorld; }

private:
  static World* mWorld;

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

  FifoMsg     mFifoMsg;
  char*       mCompletionMessage = nullptr;
  void*       mReplyAddr = nullptr;
  const char* mName = nullptr;
  index       checkThreadInterval;
  index       pollCounter{0};
  index       mPreviousTrigger{0};
  bool        mSynchronous{true};
  Result      mResult;
  
  static CommandMap  mCommandDispatchTable;
};


template <typename Client, typename Wrapper>
World* NonRealTime<Client, Wrapper>::mWorld{nullptr};

template <typename Client, typename Wrapper>
typename NonRealTime<Client, Wrapper>::Cache
    NonRealTime<Client, Wrapper>::mCache{};
    
template <typename Client, typename Wrapper>
CommandMap NonRealTime<Client, Wrapper>::mCommandDispatchTable{};


} // namespace impl
} // namespace client
} // namespace fluid
