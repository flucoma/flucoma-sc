#pragma once


#include "data/FluidTensor.hpp"
#include "clients/common/FluidParams.hpp"

#include "SC_PlugIn.h"

#include <boost/align/aligned_alloc.hpp>
#include <vector>


//static InterfaceTable *ft;

namespace fluid {
namespace sc{
  /**
   A descendent of SndBuf that will populate itself
   from the NRT mirror buffers given a world and a bufnum
   **/
  struct NRTBuf: public SndBuf
  {
    NRTBuf(SndBuf& b):SndBuf(b){}
    NRTBuf(World* world,long bufnum):
    NRTBuf(*World_GetNRTBuf(world,bufnum))
    {
      if(!this->samplerate)
        this->samplerate = world->mFullRate.mSampleRate;
      
    }
  };
  
  /**
   A combination of SndBuf and parameter::BufferAdaptor (which, in turn, exposes FluidTensorView<float,2>), for simple transfer of data
   
   Given a World* and a buffer number, this will populate its SndBuf stuff
   from the NRT mirror buffers, and create a FluidTensorView wrapper of
   appropriate dimensions.
   
   The SndBuf can then be 'transferred' back to the RT buffers once we're done with it,
   and SC notified of the update. (In the context of SequencedCommands, in which this is meant
   to be used, this would happen at Stage3() on the real-time thread)
   
   nSamps = rows
   nChans = columns
   **/
  class SCBufferView: public NRTBuf, public parameter::BufferAdaptor
  {
  public:
    SCBufferView() = delete;
    SCBufferView(SCBufferView&) = delete;
    SCBufferView operator=(SCBufferView&) = delete;
    
    SCBufferView(long bufnum,World* world):
      NRTBuf(world,bufnum),
//      BufferAdaptor({0,{static_cast<size_t>(frames),static_cast<size_t>(channels)}},NRTBuf::data),
      mBufnum(bufnum), mWorld(world)
    {
      mChans = this->channels;
      mFrames = this->frames;
      
    }
    
    ~SCBufferView() = default;
    
    void assignToRT(World* rtWorld)
    {
      SndBuf* rtBuf = World_GetBuf(rtWorld,mBufnum);
      *rtBuf = static_cast<SndBuf>(*this);
      rtWorld->mSndBufUpdates[mBufnum].writes++;
    }
    //No locks in (vanilla) SC, so no-ops for these
    void acquire()  override {}
    void release()  override {}
    
    //Validity is based on whether this buffer is within the range the server knows about
    bool valid() const override {
      return (mBufnum >=0  && mBufnum < mWorld->mNumSndBufs);
    }
    
    FluidTensorView<float,1> samps(size_t channel, size_t rankIdx = 0) override
    {
      FluidTensorView<float,2>  v{this->data,0, static_cast<size_t>(mFrames),static_cast<size_t>(mChans * mRank)};
      
      return v.col(rankIdx + channel * mRank );
    }
    //Return a view of all the data
    FluidTensorView<float,2> samps() override
    {
      return {this->data,0, static_cast<size_t>(mFrames), static_cast<size_t>(mChans * mRank)};
    }
    
    //Return a 2D chunk
    FluidTensorView<float,2> samps(size_t offset, size_t nframes, size_t chanoffset, size_t chans) override
    {
      FluidTensorView<float,2>  v{this->data,0, static_cast<size_t>(mFrames), static_cast<size_t>(mChans * mRank)};
      
      return v(fluid::slice(offset,nframes), fluid::slice(chanoffset,chans));
      
      
    }
    
    
    size_t numSamps() const override
    {
      if(valid())
      {
        return this->frames;
      }
      return 0;
    }
  
    size_t numChans() const override
    {
      if(valid())
      {
        return this->channels;
      }
      return 0;
    }
    
    
    void resize(size_t frames, size_t channels, size_t rank) override {
      
      SndBuf* thisThing = static_cast<SndBuf*>(this);
      
      float* oldData = thisThing->data;
      mRank = rank;
      mWorld->ft->fBufAlloc(this, channels * rank, frames, this->samplerate);
      
      mFrames = this->frames;
      mChans = this->channels / mRank;
      
//      FluidTensorView<float,2> v=  FluidTensorView<float,2>(NRTBuf::data,0,static_cast<size_t>(frames),static_cast<size_t>(channels * rank));
//
//      static_cast<FluidTensorView<float,2>&>(*this) = std::move(v);
      
      if(oldData)
        boost::alignment::aligned_free(oldData);
      
      
    }
  protected:
    bool equal(BufferAdaptor* rhs) const override
    {
      SCBufferView* x = dynamic_cast<SCBufferView*>(rhs);
      if(x)
      {
        return mBufnum == x->mBufnum;
      }
      return false;
    }
    
    long mBufnum;
    World* mWorld;
  };
  
  
  class NRTCommandBase{
    using param_type = fluid::parameter::Instance;
  public:
    NRTCommandBase() = delete;
    NRTCommandBase(NRTCommandBase&) = delete;
    NRTCommandBase& operator=(NRTCommandBase&) = delete;
    
    NRTCommandBase(void* inUserData)
//    mWorld(inWorld),mReplyAddr(replyAddr), mCompletionMsgData(completionMsgData), mCompletionMsgSize(completionMsgSize),
    
    {}
    
    ~NRTCommandBase() = default;
    
    template <typename T> using AsyncFn = bool (T::*)(World* w);
    template <typename T> using AsyncCleanup = void (T::*)();
    
    template <typename T, AsyncFn<T> F>
    static bool call(World* w,void* x){return (static_cast<T*>(x)->*F)(w);}
    
    template<typename T>
    static void call(World*, void* x){delete static_cast<T*>(x);}
    
    template<typename T, AsyncFn<T> Stage2, AsyncFn<T> Stage3, AsyncFn<T> Stage4>
    void cmd(World* world, std::string name, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
    {
      (world->ft->fDoAsynchronousCommand)(world, replyAddr,name.c_str(),this,
                                    call<T,Stage2>, call<T,Stage3>, call<T,Stage4>,call<T>,
                                    completionMsgSize,completionMsgData);
    }

  protected:
//    World * mWorld;
//    InterfaceTable *ft;
    long bufNUm;
    void* mReplyAddr;
    const char* cmdName;
    void *cmdData;
    char* mCompletionMsgData;
    size_t mCompletionMsgSize;
//    std::vector<param_type> mParams;
  };
  
  //This wraps a class instance in a function call to pass to SC
  template<typename NRT_Plug>
  void command(World *inWorld, void* inUserData, struct sc_msg_iter *args, void *replyAddr)
  {
    
    NRT_Plug* cmd = new NRT_Plug(inUserData);
    
    //Iterate over parameter descriptions associated with this client object, fill with data from language side
//    std::vector<parameter::Instance> params = NRT_Plug::client_type::newParameterSet();
    for (auto&& p: cmd->parameters())
    {
      switch(p.getDescriptor().getType())
      {
        case parameter::Type::Buffer:
        {
          long bufNum = static_cast<long>(args->geti());
          if(bufNum >= 0){
            SCBufferView* buf = new SCBufferView(bufNum,inWorld);
            p.setBuffer(buf);
          }
          break;
        }
        case parameter::Type::Long:
        {
          p.setLong(static_cast<long>(args->geti()));
          break;
        }
        case parameter::Type::Float:
        {
          p.setFloat(args->getf());
          break;
        }
        default:
        {
          p.setLong(static_cast<long>(args->geti()));
        }
      }
    }
    
    //Deal with the completion message at the end, if any
    size_t completionMsgSize = args->getbsize();
    char* completionMsgData = 0;
    if(completionMsgSize)
    {
      //allocate string
      completionMsgData = (char*)inWorld->ft->fRTAlloc(inWorld,completionMsgSize);
      args->getb(completionMsgData,completionMsgSize);
    }
    //Make a new pointer for our plugin, and set it going
    
    cmd->runCommand(inWorld, replyAddr, completionMsgData, completionMsgSize);
  }
} //namespace sc
}//namespace fluid


template <typename NRT_Plug,typename NRT_Client>
void registerCommand(InterfaceTable* ft, const char* name)
{
  PlugInCmdFunc cmd =  fluid::sc::command<NRT_Plug>;
  (*ft->fDefinePlugInCmd)(name,cmd,nullptr);
}

