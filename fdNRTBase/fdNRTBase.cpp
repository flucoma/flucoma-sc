//Can I reallocate buffers on the server? Yes I can.
#include "SC_PlugIn.h"
#include <vector>
#include "data/FluidTensor.hpp"

static InterfaceTable *ft;

namespace fluid {
namespace sc{
    using ViewType = fluid::FluidTensorView<float, 2>;
    
    /**
     A descendent of SndBuf that will populate itself
     from the NRT mirror buffers given a world and a bufnum
     **/
    struct NRTBuf: public SndBuf
    {
        NRTBuf(SndBuf& b):SndBuf(b){}
        
        NRTBuf(World* world,size_t bufnum):
            NRTBuf(*World_GetNRTBuf(world,bufnum))
        {}
    };
    
    /**
     A combination of SndBuf and FluidTensorView, for simple transfer of data
     
     Given a World* and a buffer number, this will populate its SndBuf stuff
     from the NRT mirror buffers, and create a FluidTensorView wrapper of
     appropriate dimensions.
     
     The SndBuf can then be 'transferred' back to the RT buffers once we're done with it,
     and SC notified of the update. (In the context of SequencedCommands, in which this is meant
     to be used, this would happen at Stage3() on the real-time thread)

    **/
    class SCBufferView: public NRTBuf,ViewType
    {
    public:
        SCBufferView() = delete;
        SCBufferView(SCBufferView&) = delete;
        SCBufferView operator=(SCBufferView&) = delete;
        
        SCBufferView(size_t bufnum,World* world):
            NRTBuf(world,bufnum),
            ViewType({0,{static_cast<size_t>(frames),
                         static_cast<size_t>(channels)}},NRTBuf::data),
            mBufnum(bufnum), mWorld(world)
        {}
        
        void assignToRT()
        {
            SndBuf* rtBuf = World_GetBuf(mWorld,mBufnum);
            *rtBuf = static_cast<SndBuf>(*this);
            mWorld->mSndBufUpdates[mBufnum].writes++;
        }
        
    private:
        size_t mBufnum;
        World * mWorld;
    };
    
    class NRTCommandBase{
        
        
        template <typename T>
        using AsyncFn = bool (T::*)();
        
        template <typename T>
        using AsyncCleanup = void (T::*) ();
        
        template<typename T, AsyncFn<T> F>
        static bool call(World*,void* x)
        {
           return (static_cast<T*>(x)->*F)();
        }
        
        template<typename T, AsyncCleanup<T> F>
        static void call(World*, void* x)
        {
            (static_cast<T*>(x)->*F)();
        }

        template<typename T, AsyncFn<T> Stage2, AsyncFn<T> Stage3, AsyncFn<T> Stage4, AsyncCleanup<T> Cleanup>
        void cmd(std::string name)
        {
            (*ft->fDoAsynchronousCommand)( mWorld, mReplyAddr,name.c_str(),this,
                                          call<T,Stage2>, call<T,Stage3>, call<T,Stage4>,call<T,Cleanup>,
                                          mCompletionMsgSize,mCompletionMsgData);
        }
        

        
        void cm()
        {
            cmd<NRTCommandBase,&NRTCommandBase::do_processing,&NRTCommandBase::post_processing,&NRTCommandBase::dummy_stage,&NRTCommandBase::dummy_cleanup>("Bob");
        }
        
        
        
    public:
        NRTCommandBase() = delete;
        NRTCommandBase(NRTCommandBase&) = delete;
        NRTCommandBase& operator=(NRTCommandBase&) = delete;
  
        
        NRTCommandBase(size_t buffers_in, size_t buffers_out, World *inWorld, void* inUserData, struct sc_msg_iter *args, void *replyAddr):
        input_buffers(buffers_in,-1), output_buffers(buffers_out,-1),
        mWorld(inWorld),mReplyAddr(replyAddr)
        {
            for(auto&& i: input_buffers)
                i = args->geti();
            
            for(auto&& o: output_buffers)
                o = args->geti();
            
            mCompletionMsgSize = args->getbsize();
            mCompletionMsgData = 0;
            if(mCompletionMsgSize)
            {
                //allocate string
                mCompletionMsgData = (char*)RTAlloc(mWorld,mCompletionMsgSize);
                args->getb(mCompletionMsgData,mCompletionMsgSize);
            }
            
        }
        
        virtual void process()
        {
            
        }
        
    private:
        
        bool do_processing()
        {
            
            return true;
        }
        bool post_processing()
        {
            return true;
        }
        bool dummy_stage() { return true; }
        void dummy_cleanup() {}
        
        std::vector<long> input_buffers;
        std::vector<long> output_buffers;

    protected:
        World * mWorld;
        void* mReplyAddr;
        const char* cmdName;
        void *cmdData;
        size_t mCompletionMsgSize;
        char* mCompletionMsgData;
        
    };
} //namespace supercollider
}//namespace fluid


template<typename NRT_Plug>
void command(World *inWorld, void* inUserData, struct sc_msg_iter *args, void *replyAddr)
{
    NRT_Plug cmd(inWorld, inUserData, args, replyAddr);
    
    
    
}


template <typename NRT_Plug>
void registerCommand(InterfaceTable* ft, const char* name)
{
    //typedef void (*PlugInCmdFunc)(World *inWorld, void* inUserData, struct sc_msg_iter *args, void *replyAddr);
    PlugInCmdFunc cmd =  command<NRT_Plug>;
    
    
    
    (*ft->fDefinePlugInCmd)(name,cmd,nullptr);
}


//PluginLoad(BufferFunTime) {
//    
//    using fluid::sc::NRTCommandBase;
//    
//    registerCommand<NRTCommandBase>(inTable, "ASyncBufMatch");
//    
////    ft = inTable;
////    //BufGen version: all in the NRT thread
//////    DefineBufGen("BufMatch", BufferMatch);
////    //ASync version: swaps between NRT and RT threads
////    DefinePlugInCmd("AsyncBufMatch", ASyncBufferFun_Main, nullptr);
////
//}

