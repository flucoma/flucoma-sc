#pragma once


#include "data/FluidTensor.hpp"
#include "clients/common/FluidParams.hpp"

#include "SC_PlugIn.h"

#include <boost/align/aligned_alloc.hpp>
#include <cctype>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <vector>


//static InterfaceTable *ft;

namespace fluid {
namespace sc{
  /**
   A descendent of SndBuf that will populate itself
   from the NRT mirror buffers given a world and a bufnum
   **/
  struct NRTBuf//: public SndBuf
  {
    NRTBuf(SndBuf* b):mBuffer(b){}
    NRTBuf(World* world,long bufnum, bool rt=false):
    NRTBuf(rt?World_GetBuf(world, bufnum):World_GetNRTBuf(world,bufnum))
    {
      if(mBuffer && !mBuffer->samplerate)
        mBuffer->samplerate = world->mFullRate.mSampleRate;
    }
  protected:    
    SndBuf* mBuffer;
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
    
    
    SCBufferView(long bufnum,World* world,bool rt=false):
      NRTBuf(world,bufnum,rt), mBufnum(bufnum), mWorld(world)
    {
      
      
      
    }
    
    ~SCBufferView() = default;
    
    void assignToRT(World* rtWorld)
    {
      SndBuf* rtBuf = World_GetBuf(rtWorld,mBufnum);
      *rtBuf = *mBuffer;
      rtWorld->mSndBufUpdates[mBufnum].writes++;
    }
    
    void cleanUp()
    {
      if(mOldData)
        boost::alignment::aligned_free(mOldData);
    }
    
    //No locks in (vanilla) SC, so no-ops for these
    void acquire()  override {
//       NRTLock(mWorld);
    }
    void release()  override {
//      NRTUnlock(mWorld);
    }
    
    //Validity is based on whether this buffer is within the range the server knows about
    bool valid() const override {
      return (mBuffer && mBufnum >=0  && mBufnum < mWorld->mNumSndBufs);
    }
    
    bool exists() const override {
      return mBufnum >=0  && mBufnum < mWorld->mNumSndBufs; 
    }
    
    FluidTensorView<float,1> samps(size_t channel, size_t rankIdx = 0) override
    {
      FluidTensorView<float,2>  v{mBuffer->data,0, static_cast<size_t>(mBuffer->frames),static_cast<size_t>(mBuffer->channels)};
      
      return v.col(rankIdx + channel * mRank );
    }
//    //Return a view of all the data
//    FluidTensorView<float,2> samps() override
//    {
//      return {mBuffer->data,0, static_cast<size_t>(mBuffer->frames), static_cast<size_t>(mBuffer->channels)};
//    }
    
    //Return a 2D chunk
    FluidTensorView<float,1> samps(size_t offset, size_t nframes, size_t chanoffset) override
    {
      FluidTensorView<float,2>  v{mBuffer->data,0, static_cast<size_t>(mBuffer->frames), static_cast<size_t>(mBuffer->channels)};
      
      return v(fluid::slice(offset,nframes), fluid::slice(chanoffset,1)).col(0);
    }
    
    size_t numFrames() const override
    {
        return valid() ?  this->mBuffer->frames : 0 ;
    }
  
    size_t numChans() const override
    {
        return valid() ?  this->mBuffer->channels / mRank : 0;
    }
    
    size_t rank() const override
    {
        return valid() ? mRank :0;
    }
    
    void resize(size_t frames, size_t channels, size_t rank) override {
      SndBuf* thisThing = mBuffer;
      mOldData = thisThing->data;
      mRank = rank;
      mWorld->ft->fBufAlloc(mBuffer, channels * rank, frames, thisThing->samplerate);
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
    
    float* mOldData = 0;
    long mBufnum;
    World* mWorld;
    size_t mRank = 1;
  };
  
  class RTBufferView: public parameter::BufferAdaptor
  {
  public:
    RTBufferView(World* world, int bufnum): mWorld(world), mBufnum(bufnum) {}
    
    void acquire()  override {
      mBuffer = World_GetBuf(mWorld, mBufnum);
    }
    void release()  override {
      //      NRTUnlock(mWorld);
    }
    
    //Validity is based on whether this buffer is within the range the server knows about
    bool valid() const override {
      return (mBuffer && mBufnum >=0 && mBufnum < mWorld->mNumSndBufs);
    }
    
    bool exists() const override {
      return mBufnum >=0 && mBufnum < mWorld->mNumSndBufs;
    }
    
    FluidTensorView<float,1> samps(size_t channel, size_t rankIdx = 0) override
    {
      FluidTensorView<float,2>  v{mBuffer->data,0, static_cast<size_t>(mBuffer->frames),static_cast<size_t>(mBuffer->channels)};
      
      return v.col(rankIdx + channel * mRank );
    }
 
    FluidTensorView<float,1> samps(size_t offset, size_t nframes, size_t chanoffset) override
    {
      FluidTensorView<float,2>  v{mBuffer->data,0, static_cast<size_t>(mBuffer->frames), static_cast<size_t>(mBuffer->channels)};
      
      return v(fluid::slice(offset,nframes), fluid::slice(chanoffset,1)).col(0);
    }
    
    size_t numFrames() const override
    {
      return valid() ?  this->mBuffer->frames : 0 ;
    }
    
    size_t numChans() const override
    {
      return valid() ?  this->mBuffer->channels / mRank : 0;
    }
    
    size_t rank() const override
    {
      return valid() ? mRank :0;
    }
    
    void resize(size_t frames, size_t channels, size_t rank) override {
      assert(false && "Don't try and resize real-time buffers");
//      SndBuf* thisThing = mBuffer;
//      mOldData = thisThing->data;
//      mRank = rank;
//      mWorld->ft->fBufAlloc(mBuffer, channels * rank, frames, thisThing->samplerate);
    }
    
    int bufnum() {
      return mBufnum;
    }
    

  private:
    
    bool equal(BufferAdaptor* rhs) const override
    {
      RTBufferView* x = dynamic_cast<RTBufferView*>(rhs);
      if(x)
      {
        return mBufnum == x->mBufnum;
      }
      return false;
    }
    
    size_t mRank = 1;
    World* mWorld;
    int mBufnum = -1;
    SndBuf* mBuffer = nullptr;
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
  
  template<typename Client>
  static void printCmd(InterfaceTable* ft, const char* name, const char* classname)
  {
    
    
    
//    std::string filepath(__FILE__);
//    size_t path_sep = filepath.rfind('/');
//    size_t extdot = filepath.rfind('.');
//    filepath.erase(filepath.begin() + extdot,filepath.end());
//    filepath.erase(filepath.begin(),filepath.begin() + path_sep + 1);
//    std::for_each(filepath.begin(), filepath.begin() + 2, [](char& c){
//      c = std::toupper(c);
//    });
    
    std::string filepath("/tmp/");
    filepath.append(classname);
    filepath.append(".sc");
    
    std::ofstream ss(filepath);
    ss << classname << "{\n";
    
    ss << "\t\t*process { arg server";
    
    std::ostringstream cmd;
    cmd << "\t\t\tserver.sendMsg(\\cmd, \\" << name;
    
    size_t count = 0;
    for(auto&& d: Client::getParamDescriptors())
    {
      ss << ", " << d.getName();
      if(d.hasDefault())
      {
        ss << " = " << d.getDefault();
      }
      
      cmd << ", ";
      if(d.getType() == parameter::Type::Buffer)
      {
        if (count == 0)
          cmd <<  d.getName() << ".bufnum";
        else
          cmd << "\nif( " << d.getName() << ".isNil, -1, {" << d.getName() << ".bufnum})";
      }
      else
        cmd << d.getName();
      count++;
    }
    
    cmd << ");\n\n";
    
    ss << ";\n\n\t\tserver = server ? Server.default\n;" ;
    
    if(Client::getParamDescriptors()[0].getType() == parameter::Type::Buffer)
    {
      ss << "if("<<Client::getParamDescriptors()[0].getName()
      << ".bufnum.isNil) {Error(\"Invalid Buffer\").format(thisMethod.name, this.class.name).throw};\n\n";
    }
    
    ss << cmd.str() << "\n\n}\n}";
    
//    Print(ss.str().c_str());
  }
  
  
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
          long bufnum = static_cast<long>(args->geti());
          if(bufnum >= 0){
            SCBufferView* buf = new SCBufferView(bufnum,inWorld);
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

