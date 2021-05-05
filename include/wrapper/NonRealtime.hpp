#pragma once 

#include "BufferFuncs.hpp"
#include "CopyReplyAddress.hpp"
#include "Messaging.hpp"
#include "Meta.hpp"
#include "RealTimeBase.hpp"
#include "SCBufferAdaptor.hpp"
#include <clients/common/FluidBaseClient.hpp>
#include <data/FluidMeta.hpp>
#include <SC_PlugIn.hpp>
#include <scsynthsend.h>
#include <unordered_map>

namespace fluid {
namespace client {
namespace impl { 
  
  /// Non Real Time Processor
  
  template <typename Client, typename Wrapper>
  class NonRealTime : public SCUnit
  {
    using Params =  typename Client::ParamSetType;
    
    template<typename T,typename...Args>
    static T* rtalloc(World* world,Args&&...args)
    {
      void* space = getInterfaceTable()->fRTAlloc(world, sizeof(T));
      return new (space) T{std::forward<Args>(args)...};
    }
    
    /// Instance cache
    struct CacheEntry
    {
      
      CacheEntry(const Params& p):mParams{p},mClient{mParams}
      {}
      
      Params mParams;
      Client mClient;
      bool mDone{false};
    };
    
    using CacheEntryPointer = std::shared_ptr<CacheEntry>; 
    using WeakCacheEntryPointer = std::weak_ptr<CacheEntry>; //could use weak_type in 17

   public:
    using Cache = std::unordered_map<index,CacheEntryPointer>; 
    static Cache mCache;
   private:
    static bool isNull(WeakCacheEntryPointer const& weak) {
      return !weak.owner_before(WeakCacheEntryPointer{}) && !WeakCacheEntryPointer{}.owner_before(weak);
    }
    
    // https://rigtorp.se/spinlock/
    struct Spinlock {
      std::atomic<bool> lock_ = {0};
      
      void lock() noexcept {
        for (;;) {
          // Optimistically assume the lock is free on the first try
          if (!lock_.exchange(true, std::memory_order_acquire)) {
            return;
          }
          // Wait for lock to be released without generating cache misses
          while (lock_.load(std::memory_order_relaxed)) {
            // Issue X86 PAUSE or ARM YIELD instruction to reduce contention between
            // hyper-threads
            //__builtin_ia32_pause();
          }
        }
      }
      
      bool tryLock() noexcept {
        // First do a relaxed load to check if lock is free in order to prevent
        // unnecessary cache misses if someone does while(!try_lock())
        return !lock_.load(std::memory_order_relaxed) &&
        !lock_.exchange(true, std::memory_order_acquire);
      }
      
      void unlock() noexcept {
        lock_.store(false, std::memory_order_release);
      }
    };
    
    //RAII for above
    struct ScopedSpinlock
    {
      ScopedSpinlock(Spinlock& _l) noexcept: mLock{_l}
      {
        mLock.lock();
      }
      ~ScopedSpinlock() { mLock.unlock(); }
      private:
        Spinlock& mLock;
    };
  
    static Spinlock mSpinlock;
    
    // shouldn't be called without at least *thinking* about getting spin lock first 
    static inline WeakCacheEntryPointer unsafeGet(index id)
    {
        auto   lookup  = mCache.find(id);
        return lookup == mCache.end() ? WeakCacheEntryPointer() : lookup->second;
    }
  
  public:
    static WeakCacheEntryPointer get(index id)
    {
      ScopedSpinlock{mSpinlock};
      return unsafeGet(id);
    }

    static WeakCacheEntryPointer tryGet(index id)
    {
        if(mSpinlock.tryLock())
        {
          auto ret = unsafeGet(id);
          mSpinlock.unlock();
          return ret;
        }
        return WeakCacheEntryPointer{};
    }


    static WeakCacheEntryPointer add(index id, const Params& params)
    {
      ScopedSpinlock{mSpinlock};
      if(isNull(get(id)))
      {
        auto result =  mCache.emplace(id,
                               std::make_shared<CacheEntry>(params));
                               
        return result.second ? (result.first)->second : WeakCacheEntryPointer(); //sob
      }
      else //client has screwed up
      {
          std::cout << "ERROR: " <<  Wrapper::getName() << " ID " << id << " already in use\n";
          return {};
      }
    }
    
    static void remove(index id)
    {
      ScopedSpinlock{mSpinlock};
      mCache.erase(id);
    }
    
    static void printNotFound(index id)
    {
      std::cout << "ERROR: " << Wrapper::getName() << " no instance with ID " << id << std::endl;
    }
    
  private:
    static InterfaceTable* getInterfaceTable() { return Wrapper::getInterfaceTable() ;}
    
    template <size_t N, typename T>
    using ParamsFromOSC = typename ClientParams<Wrapper>::template Setter<sc_msg_iter, N, T>;

    template <size_t N, typename T>
    using ParamsFromSynth = typename ClientParams<Wrapper>::template Setter<impl::FloatControlsIter, N, T>;


    struct NRTCommand
    {
      NRTCommand(World*, sc_msg_iter* args, void* replyAddr, bool consumeID = true)
      {
        auto count = args->count;
        auto pos = args->rdpos;
        
        mID = args->geti();
        
        if(!consumeID)
        {
          args->count = count;
          args->rdpos = pos;
        }
        
        if(replyAddr)
         mReplyAddress =  copyReplyAddress(replyAddr);
      }
      
      ~NRTCommand()
      {
        if(mReplyAddress) deleteReplyAddress(mReplyAddress);
      }
      
      NRTCommand(){}
      
      explicit NRTCommand(index id):mID{id}{}
      
      bool stage2(World*) { return true; } //nrt
      bool stage3(World*) { return true; } //rt
      bool stage4(World*) { return false; } //nrt
      void cleanup(World*) {} //rt
                    
      void sendReply(const char* name,bool success)
      {
        if(mReplyAddress)
        {
          std::string slash{"/"};
          small_scpacket packet;
          packet.adds((slash+name).c_str());
          packet.maketags(3);
          packet.addtag(',');
          packet.addtag('i');
          packet.addtag('i');
          packet.addi(success);
          packet.addi(static_cast<int>(mID));
          
          SendReply(mReplyAddress,packet.data(), static_cast<int>(packet.size()));
        }
      }
//      protected:
      index mID;
      void* mReplyAddress{nullptr};
    };
    
    struct CommandNew : public NRTCommand
    {
      CommandNew(World* world, sc_msg_iter* args,void* replyAddr)
        : NRTCommand{world,args, replyAddr, !IsNamedShared_v<Client>},
          mParams{Client::getParameterDescriptors()}
      {
        mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr, world, *args);
      }

      CommandNew(index id, World*, FloatControlsIter& args, Unit* x)
        :NRTCommand{id},
         mParams{Client::getParameterDescriptors()}
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
//        auto entry = ;


        Result constraintsRes =  validateParameters(mParams);

        if(!constraintsRes.ok()) Wrapper::printResult(w,constraintsRes);

        mResult = (!isNull(add(NRTCommand::mID, mParams)));
                
        //Sigh. The cache entry above has both the client instance and main params instance.
        // The client is linked to the params by reference; I've not got the in-place constrction
        // working properly so that params are in their final resting place by the time we make the client
        // so (for) now we need to manually repoint the client to the correct place. Or badness.
        if(mResult)
        {
           auto ptr = get(NRTCommand::mID).lock();
           ptr->mClient.setParams(ptr->mParams);
        }
        
        NRTCommand::sendReply(name(),mResult);
        
        return mResult;
      }
      
    private:
      bool mResult;
      Params mParams;
    };
    
    struct CommandFree: public NRTCommand
    {
      using NRTCommand::NRTCommand;
      
      void cancelCheck(std::false_type, index id)
      {
        if(auto ptr = get(id).lock())
        {
          auto& client = ptr->mClient;
          if(!client.synchronous() && client.state() == ProcessState::kProcessing)
            std::cout << Wrapper::getName()
            << ": Processing cancelled"
            << std::endl;
        }
      }
      
      void cancelCheck(std::true_type, index){}
      
            
      static const char* name()
      {
        static std::string cmd = std::string(Wrapper::getName()) + "/free";
        return cmd.c_str();
      }
      
      bool stage2(World*)
      {
        cancelCheck(IsRTQueryModel_t(),NRTCommand::mID);
        remove(NRTCommand::mID);
        NRTCommand::sendReply(name(), true);
        return true;
      }

    };
    
    
    /// Not registered as a PlugInCmd. Triggered by  worker thread callback
    struct CommandAsyncComplete: public NRTCommand
    {
      CommandAsyncComplete(World*, index id, void* replyAddress)
      {
        NRTCommand::mID = id;
        NRTCommand::mReplyAddress = replyAddress;
      }
      
      static const char* name() { return CommandProcess::name(); }
      
      bool stage2(World* world)
      {
        
        // std::cout << "In Async completion\n";
        if(auto ptr = get(NRTCommand::mID).lock())
        {
          Result       r;
          mRecord = ptr;
          auto& client = ptr->mClient;
          ProcessState s = client.checkProgress(r);
          if (s == ProcessState::kDone || s == ProcessState::kDoneStillProcessing)
          {
            if (r.status() == Result::Status::kCancelled)
            {
              std::cout << Wrapper::getName()
                        << ": Processing cancelled"
                        << std::endl;
              ptr->mDone = true;
              return false;
            }
            
            client.checkProgress(r);
            mSuccess = !(r.status() == Result::Status::kError);
            if (!r.ok())
            {
              Wrapper::printResult(world,r);              
              if(r.status() == Result::Status::kError)
              {
                ptr->mDone = true;
                return false;
              }
            }
            
            return true;
          }
        }
        return false;
      }
      
      bool stage3(World* world)
      {
        if(auto ptr = mRecord.lock())
        {
          auto& params = ptr->mParams;
          params.template forEachParamType<BufferT, AssignBuffer>(world);
          return true;
        }
        return false;
      }
      
      bool stage4(World*) //nrt
      {
        if(auto ptr = get(NRTCommand::mID).lock())
        {
          ptr->mParams.template forEachParamType<BufferT, impl::CleanUpBuffer>();
          
          if(NRTCommand::mID >= 0 && NRTCommand::mReplyAddress)
          {
            NRTCommand::sendReply(name(),mSuccess);
          }
          ptr->mDone = true;
          return true;
        }
        return false;
      }
      
      bool mSuccess;
      WeakCacheEntryPointer mRecord;
    };
    
        
    static void doProcessCallback(World* world, index id,size_t completionMsgSize,char* completionMessage,void* replyAddress)
    {
      auto ft = getInterfaceTable();
      struct Context{
        World* mWorld;
        index  mID;
        size_t    mCompletionMsgSize;
        char*  mCompletionMessage;
        void*  mReplyAddress;
      };
      
      Context* c = new Context{world,id,completionMsgSize,completionMessage,replyAddress};
      
      auto launchCompletionFromNRT = [](FifoMsg* inmsg)
      {        
        auto runCompletion = [](FifoMsg* msg){
          Context* c = static_cast<Context*>(msg->mData);
          World* world = c->mWorld;
          index id = c->mID;
          auto ft = getInterfaceTable();
          void* space = ft->fRTAlloc(world,sizeof(CommandAsyncComplete));
          CommandAsyncComplete* cmd = new (space) CommandAsyncComplete(world, id,c->mReplyAddress);
          runAsyncCommand(world, cmd, c->mReplyAddress, c->mCompletionMsgSize, c->mCompletionMessage);
          if(c->mCompletionMsgSize) ft->fRTFree(world,c->mCompletionMessage); 
        };
        
        auto tidyup = [](FifoMsg* msg)
        {
          Context* c = static_cast<Context*>(msg->mData);
          delete c;
        };
        
        auto ft = getInterfaceTable();
        FifoMsg fwd = *inmsg;
        fwd.Set(inmsg->mWorld, runCompletion, tidyup, inmsg->mData);
        if(inmsg->mWorld->mRunning)
          ft->fSendMsgToRT(inmsg->mWorld,fwd);
      };
      
      FifoMsg msg;
      msg.Set(world, launchCompletionFromNRT, nullptr, c);
      
      if(world->mRunning) ft->fSendMsgFromRT(world,msg);
    }
        
    struct CommandProcess: public NRTCommand
    {
      CommandProcess(World* world, sc_msg_iter* args, void* replyAddr): NRTCommand{world, args, replyAddr},mParams{Client::getParameterDescriptors()}
      {
        auto& ar = *args;
        
        if(auto ptr = get(NRTCommand::mID).lock())
        {
           ptr->mDone = false;
           mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr, world, ar);
           mSynchronous = static_cast<bool>(ar.geti());
        } //if this fails, we'll hear about it in stage2 anyway
      }

      explicit CommandProcess(index id,bool synchronous,Params* params):NRTCommand{id},mSynchronous(synchronous),
      mParams{Client::getParameterDescriptors()}
      {
       if(params)
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
        if(auto ptr = mRecord.lock())
        {
                    
          auto& params = ptr->mParams;
          if(mOverwriteParams) params = mParams;
          auto& client = ptr->mClient;
          

          
//          if(mOSCData)
//          {
//             params.template setParameterValuesRT<ParamsFromOSC>(nullptr, world, *mOSCData);
//             mSynchronous = static_cast<bool>(mOSCData->geti());
//          }
                    
          Result result = validateParameters(params);
          Wrapper::printResult(world, result);
          if (result.status() != Result::Status::kError)
          {
//            client.done()
            client.setSynchronous(mSynchronous);
            index id = NRTCommand::mID;
            size_t completionMsgSize = mCompletionMsgSize;
            char* completionMessage = mCompletionMessage;
            void* replyAddress = copyReplyAddress(NRTCommand::mReplyAddress);
            
            auto callback = [world,id,completionMsgSize,completionMessage,replyAddress](){
              doProcessCallback(world,id,completionMsgSize,completionMessage,replyAddress);
            };
                    
            result = mSynchronous ? client.enqueue(params) : client.enqueue(params,callback);
            Wrapper::printResult(world, result);
            
            if(result.ok())
            {
              ptr->mDone = false;
              mResult = client.process();
              Wrapper::printResult(world,mResult);
              
              bool error =mResult.status() == Result::Status::kError;
              
              if(error) ptr->mDone = true;
              return mSynchronous && !error;
            }
          }
        }
        else
        {
          mResult = Result{Result::Status::kError, "No ", Wrapper::getName(), " with ID ", NRTCommand::mID};
          Wrapper::printResult(world,mResult);
        }
        return false;
      }

      //Only for blocking execution
      bool stage3(World* world) //rt
      {
        if(auto ptr = mRecord.lock())
        {
          ptr->mParams.template forEachParamType<BufferT, AssignBuffer>(world);
//          NRTCommand::sendReply(world, name(), mResult.ok());
          return true;
        }
        // std::cout << "Ohno\n";
        return false;
      }
      
      //Only for blocking execution
      bool stage4(World*) //nrt
      {
        if(auto ptr = get(NRTCommand::mID).lock())
        {
          ptr->mParams.template forEachParamType<BufferT, impl::CleanUpBuffer>();
          
          if(NRTCommand::mID >= 0 && mSynchronous)
            NRTCommand::sendReply(name(), mResult.ok());
          ptr->mDone = true;
          return true;
        }
        return false;
      }
      

      bool synchronous()
      {
        return mSynchronous;
      }

      void addCompletionMessage(size_t size, char* message)//, void* addr)
      {
        mCompletionMsgSize = size;
        mCompletionMessage = message;
      }
       
//      private:
        Result mResult;
        bool mSynchronous;
        size_t mCompletionMsgSize{0};
        char* mCompletionMessage{nullptr};
        Params mParams;
        bool mOverwriteParams{false};
        WeakCacheEntryPointer mRecord;
    };
    
    struct CommandProcessNew: public NRTCommand
    {
      CommandProcessNew(World* world, sc_msg_iter* args,void* replyAddr)
       : mNew{world, args, replyAddr},
         mProcess{mNew.mID,false,nullptr}
     {
        mProcess.mSynchronous = args->geti();
        mProcess.mReplyAddress = mNew.mReplyAddress;
     }
      
      CommandProcessNew(index id, World* world, FloatControlsIter& args, Unit* x)
        : mNew{id, world, args, x},
          mProcess{id}
      {}
              
      static const char* name()
      {
        static std::string cmd = std::string(Wrapper::getName()) + "/processNew";
        return cmd.c_str();
      }
      
      bool stage2(World* world)
      {
        return mNew.stage2(world) ? mProcess.stage2(world) : false;
      }
      
      bool stage3(World* world) //rt
      {
        return mProcess.stage3(world);
      }

      bool stage4(World* world) //nrt
      {
        return mProcess.stage4(world);
      }
      
      void cleanup(World* world)
      {
        mProcess.mReplyAddress = nullptr; 
        mProcess.cleanup(world);
      }
      
      bool synchronous()
      {
        return mProcess.synchronous();
      }
      
      void addCompletionMessage(size_t size, char* message)
      {
        mProcess.addCompletionMessage(size, message);
      }

    private:
        CommandNew mNew;
        CommandProcess mProcess;
    };
    
    
    struct CommandCancel: public NRTCommand
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
        if(auto ptr = get(NRTCommand::mID).lock())
        {
          auto& client = ptr->mClient;
          if(!client.synchronous())
          {
            client.cancel();
            return true;
          }
        }
        return false;
      }
    };
    
    struct CommandSetParams: public NRTCommand
    {
      CommandSetParams(World* world, sc_msg_iter* args, void* replyAddr)
        : NRTCommand{world, args, replyAddr}
      {
        auto& ar = *args;
        if(auto ptr = get(NRTCommand::mID).lock())
        {
           ptr->mParams.template setParameterValuesRT<ParamsFromOSC>(nullptr, world, ar);
           Result result = validateParameters(ptr->mParams);
           ptr->mClient.setParams(ptr->mParams);
        } else printNotFound(NRTCommand::mID);
      }
      
      static const char* name()
      {
        static std::string cmd = std::string(Wrapper::getName()) + "/setParams";
        return cmd.c_str();
      }
    };
    
        
    template<typename Command>
    static auto runAsyncCommand(World* world, Command* cmd, void* replyAddr,
                                      size_t completionMsgSize, char* completionMsgData)
    {
          auto ft = getInterfaceTable();

          return ft->fDoAsynchronousCommand(world, replyAddr,Command::name(),cmd,
          [](World* w, void* d) { return static_cast<Command*>(d)->stage2(w); },
          [](World* w, void* d) { return static_cast<Command*>(d)->stage3(w); },
          [](World* w, void* d) { return static_cast<Command*>(d)->stage4(w); },
          [](World* w, void* d)
          {
              auto cmd = static_cast<Command*>(d);
              cmd->cleanup(w);
              cmd->~Command();
              getInterfaceTable()->fRTFree(w,d);
          },
          static_cast<int>(completionMsgSize), completionMsgData);
    }
    
   
    static auto runAsyncCommand(World* world, CommandProcess* cmd, void* replyAddr,
                                      size_t completionMsgSize, char* completionMsgData)
    {
          if(!cmd->synchronous())
          {
            
            auto msgcopy = (char*)getInterfaceTable()->fRTAlloc(world,completionMsgSize);
            memcpy(msgcopy, completionMsgData, completionMsgSize);
            cmd->addCompletionMessage(completionMsgSize,msgcopy);
            return runAsyncCommand<CommandProcess>(world, cmd, replyAddr, 0, nullptr);
          }
          else return runAsyncCommand<CommandProcess>(world, cmd, replyAddr, completionMsgSize, completionMsgData);
    }
   
    static auto runAsyncCommand(World* world, CommandProcessNew* cmd, void* replyAddr,
                                      size_t completionMsgSize, char* completionMsgData)
    {
          if(!cmd->synchronous())
          {
            auto msgcopy = (char*)getInterfaceTable()->fRTAlloc(world,completionMsgSize);
            memcpy(msgcopy, completionMsgData, completionMsgSize);
            cmd->addCompletionMessage(completionMsgSize,msgcopy);
            return runAsyncCommand<CommandProcessNew>(world, cmd, replyAddr, 0, nullptr);
          }
          else return runAsyncCommand<CommandProcessNew>(world, cmd, replyAddr, completionMsgSize, completionMsgData);
    }
  
  
    template<typename Command>
    static void defineNRTCommand()
    {
      auto ft = getInterfaceTable();
      auto commandRunner = [](World* world, void*, struct sc_msg_iter* args, void* replyAddr)
      {
          
          auto ft = getInterfaceTable();
          void* space = ft->fRTAlloc(world,sizeof(Command));
          Command* cmd = new (space) Command(world, args, replyAddr);
          //This is brittle, but can't think of something better offhand
          //This is the only place we can check for a completion message at the end of the OSC packet
          //beause it has to be passed on to DoAsynhronousCommand at this point. However, detecting correctly
          //relies on the Command type having fully consumed arguments from the args iterator in the constructor for cmd
          size_t completionMsgSize{args ? args->getbsize() : 0};
          assert(completionMsgSize <= std::numeric_limits<int>::max());
          char* completionMsgData = nullptr;
          
          if (completionMsgSize) {
            completionMsgData = (char*)ft->fRTAlloc(world, completionMsgSize);
            args->getb(completionMsgData, completionMsgSize);
          }
          runAsyncCommand(world, cmd, replyAddr, completionMsgSize, completionMsgData);
          
          if(completionMsgSize) ft->fRTFree(world, completionMsgData); 
          
      };
      ft->fDefinePlugInCmd(Command::name(),commandRunner,nullptr);
    }
    
    
    
    struct NRTProgressUnit: SCUnit
    {
    
      static const char* name()
      {
        static std::string n = std::string(Wrapper::getName()) + "Monitor";
        return n.c_str();
      }
    
      NRTProgressUnit()
      {
        mInterval = static_cast<index>(0.02 / controlDur());
        set_calc_function<NRTProgressUnit, &NRTProgressUnit::next>();
        Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
      }
      
      void next(int)
      {
        if (0 == mCounter++)
        {
          index id = static_cast<index>(mInBuf[0][0]);
          if(auto ptr = tryGet(id).lock())
          {
            mInit = true;
            if(ptr->mClient.done()) mDone = 1;
            out0(0) = static_cast<float>(ptr->mClient.progress());
          }
          else
          {
            if(!mInit)
              std::cout << "WARNING: No " << Wrapper::getName() << " with ID " << id << std::endl;
            else mDone = 1;
          }
        }
        mCounter %= mInterval;
      }
      
    private:
      index mInterval;
      index mCounter{0};
      bool mInit{false};
    };
    
    
    struct NRTTriggerUnit: SCUnit
    {
      
      static index count(){
        static index counter = -1;
        return counter--;
      }
      
      index ControlOffset() { return mSpecialIndex + 1; }
      
      index ControlSize()
      {
        return index(mNumInputs)
                - mSpecialIndex //used for oddball cases
                - 3; //id + trig + blocking;
      }
      
      static const char* name()
      {
        static std::string n = std::string(Wrapper::getName()) + "Trigger";
        return n.c_str();
      }
      
      NRTTriggerUnit()
      : mControlsIterator{mInBuf + ControlOffset(),ControlSize()},mParams{Client::getParameterDescriptors()}
      {
        mID = static_cast<index>(mInBuf[0][0]);
        if(mID == -1) mID = count();
        auto cmd = NonRealTime::rtalloc<CommandNew>(mWorld,mID,mWorld, mControlsIterator, this);
        runAsyncCommand(mWorld, cmd, nullptr, 0, nullptr);
//        mInst = get(mID);
        set_calc_function<NRTTriggerUnit, &NRTTriggerUnit::next>();
        Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
      }
      
      ~NRTTriggerUnit()
      {
//        if(auto ptr = mInst.lock())
//        {
          auto cmd = NonRealTime::rtalloc<CommandFree>(mWorld,mID);
          runAsyncCommand(mWorld, cmd, nullptr, 0, nullptr); 
//        }
      }
      
      void next(int)
      {
      
        
        index triggerInput = static_cast<index>(mInBuf[static_cast<index>(mNumInputs) - 2][0]);
        mTrigger = mTrigger || triggerInput;
        
//        if(auto ptr = mInst->lock())
//        if(auto ptr = get(mID).lock())
//        {
          bool  trigger = (!mPreviousTrigger) && triggerInput;//mTrigger;
          mPreviousTrigger = triggerInput;
          mTrigger = 0;
//          auto& client = ptr->mClient;
                  
          if(trigger)
          {
            mControlsIterator.reset(1 + mInBuf); //add one for ID
//            auto& params = ptr->mParams;
            Wrapper::setParams(this,mParams,mControlsIterator,true,false);
            bool blocking = mInBuf[mNumInputs - 1][0] > 0;
            CommandProcess* cmd = rtalloc<CommandProcess>(mWorld,mID,blocking,&mParams);
            runAsyncCommand(mWorld,cmd, nullptr,0, nullptr);
            mRunCount++;
          }
          else
          {
              if(auto ptr = tryGet(mID).lock())
              {
                mInit = true;
                auto& client = ptr->mClient;
                mDone = ptr->mDone;
                out0(0) = mDone ? 1 : static_cast<float>(client.progress());
              } else mDone = mInit;
          }
//        }
//        else printNotFound(id);
      }
      
    private:
      bool mPreviousTrigger{false};
      bool mTrigger{false};
      Result mResult;
      impl::FloatControlsIter mControlsIterator;
      index mID;
      index mRunCount{0};
      WeakCacheEntryPointer mInst;
      Params mParams;
      bool mInit{false};
    };
    
    struct NRTModelQueryUnit: SCUnit
    {
      using Delegate = impl::RealTimeBase<Client,Wrapper>;
      
      index ControlOffset() { return mSpecialIndex + 2; }
      index ControlSize()
      { 
        return index(mNumInputs)
                - mSpecialIndex //used for oddball cases
                - 2; // trig + id
      }
      
      static const char* name()
      {
        static std::string n = std::string(Wrapper::getName()) + "Query";
        return n.c_str();
      }
            
      NRTModelQueryUnit()
        //Offset controls by 1 to account for ID
      : mControls{mInBuf + ControlOffset(),ControlSize()}
      {
        mID = static_cast<index>(in0(1));
        init();
//        mInst = get(id);
//        if(auto ptr = mInst.lock())
//        {
//          auto& client = ptr->mClient;
//          mDelegate.init(*this,client,mControls);
          set_calc_function<NRTModelQueryUnit, &NRTModelQueryUnit::next>();
          Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);
//        }else printNotFound(mID);
      }
      
      void init()
      {
        if(mSpinlock.tryLock())
        {
           mInit = false;
           mInst =  unsafeGet(mID);
           if(auto ptr = mInst.lock())
           {
              auto& client = ptr->mClient;
              mDelegate.init(*this,client,mControls);
              mInit = true;
           }//else printNotFound(mID);
           mSpinlock.unlock();
        }
      }
      
      void next(int)
      {
                    
        index id = static_cast<index>(in0(1));
        if(mID != id) init();
        if(!mInit) return;
        if(auto ptr = mInst.lock())
        {
          auto& client = ptr->mClient;
          auto& params = ptr->mParams;
          mControls.reset(mInBuf + ControlOffset());
          mDelegate.next(*this,client,params,mControls);
        }else printNotFound(id);
      }
      
    private:
      Delegate mDelegate;
      FloatControlsIter mControls;
      index mID;
      WeakCacheEntryPointer mInst;
      bool mInit{false};
    };
    
    
    using ParamSetType = typename Client::ParamSetType;
    
    template <size_t N, typename T>
    using SetupMessageCmd = typename FluidSCMessaging<Wrapper,Client>::template SetupMessageCmd<N,T>;
  
  
    template<bool, typename CommandType>
    struct DefineCommandIf
    {
      void operator()() { }
    };


    template<typename CommandType>
    struct DefineCommandIf<true, CommandType>
    {
      void operator()() {
        // std::cout << CommandType::name() << std::endl; 
        defineNRTCommand<CommandType>();
      }
    };
    
    template<bool, typename UnitType>
    struct RegisterUnitIf
    {
      void operator()(InterfaceTable*) {}
    };

    template<typename UnitType>
    struct RegisterUnitIf<true, UnitType>
    {
      void operator()(InterfaceTable* ft) { registerUnit<UnitType>(ft,UnitType::name()); }
    };

    
    using IsRTQueryModel_t = typename Client::isRealTime;
    static constexpr bool IsRTQueryModel = IsRTQueryModel_t::value;
       
    static constexpr bool IsModel =  Client::isModelObject::value;
       
  
  public:
    static void setup(InterfaceTable* ft, const char*)
    {
      defineNRTCommand<CommandNew>();
      DefineCommandIf<!IsRTQueryModel, CommandProcess>()();
      DefineCommandIf<!IsRTQueryModel, CommandProcessNew>()();
      DefineCommandIf<!IsRTQueryModel, CommandCancel>()();
      
      DefineCommandIf<IsModel,CommandSetParams>()();
      
      defineNRTCommand<CommandFree>();
      RegisterUnitIf<!IsRTQueryModel,NRTProgressUnit>()(ft);
      RegisterUnitIf<!IsRTQueryModel,NRTTriggerUnit>()(ft);

      RegisterUnitIf<IsRTQueryModel,NRTModelQueryUnit>()(ft);
      Client::getMessageDescriptors().template iterate<SetupMessageCmd>();
      
      
      static std::string flushCmd = std::string(Wrapper::getName()) + "/flush";
      
      ft->fDefinePlugInCmd(flushCmd.c_str(),[](World*, void*, struct sc_msg_iter*, void* ){
        mCache.clear();
      },nullptr);
    }


    void init(){};

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

    FifoMsg      mFifoMsg;
    char*        mCompletionMessage = nullptr;
    void*        mReplyAddr = nullptr;
    const char*  mName = nullptr;
    index        checkThreadInterval;
    index        pollCounter{0};
    index        mPreviousTrigger{0};
    bool         mSynchronous{true};
    Result       mResult;
  };
  
  template<typename Client, typename Wrapper>
  typename NonRealTime<Client, Wrapper>::Cache  NonRealTime<Client,Wrapper>::mCache{};

  template<typename Client, typename Wrapper>
  typename NonRealTime<Client, Wrapper>::Spinlock  NonRealTime<Client,Wrapper>::mSpinlock{};


} 
}
}
