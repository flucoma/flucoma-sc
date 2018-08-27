//Can I reallocate buffers on the server? Yes I can.
#include "SC_PlugIn.h"

static InterfaceTable *ft;



//Can we do buffer resizing in a BufGen. I think so.
//Goal here is to mimic the NMF case, allowing
//the dst buffer to be resized accordingly (and not need to
// precalculate the number of frames in advance lang-side

/**
 /buf_gen approach: Seems to work, but having to do the 'swap' bewteen NRT mirror buffer and RT buffer
 in the same thread seems smelly, given how the allocation sequeneces in SC_SequenceCommand seem to work.
 **/
void BufferMatch(World *world, struct SndBuf *srcBuf, struct sc_msg_iter *msg)
{
    size_t srcFrameCount = srcBuf->frames;
    size_t srcChanCount = srcBuf->channels;
    
    long dstBufNum = msg->geti();
    size_t rank = msg->geti();
    
    if (dstBufNum == -1){
        Print("BufferMatch is not happy because there is no output buffer specified.\n");
        return;
    }
    //This sequence follows what I saw in SC_SequenceCommand.cpp. Pretty much.
    //Get the NRT thread mirror buffer
    SndBuf* dstBufNRT = World_GetNRTBuf(world, dstBufNum);
    //Call the allocation function on that
    SCErr err = ft->fBufAlloc(dstBufNRT, srcChanCount * rank, srcFrameCount,srcBuf->samplerate);
    //If we were posh, we'd check for errors
    //Norhing will happen, unless we (a) assign the allocated data back to the main buffer pool (b?) Tell the server the buffer has changed
    //Get the RT buffer
    SndBuf* dstBuf = World_GetBuf(world, dstBufNum);
    //Assign value to our NRT buffer pointer's value
    *dstBuf = *dstBufNRT;
    //Ping the server
    world->mSndBufUpdates[dstBufNum].writes ++ ;
}

/**
 ASync command version. Is this an abuse of Async command? Doesn't *seem* to be, but there is almost no
 documentation for its proper use :-|
**/
//Struct that holds all our state between stages
struct BufferFunTimeCmdData
{
    long dstbuf;
    long srcbuf;
    size_t rank;
    
    SndBuf* newdst;
};

//'Stage2()' happens in the NRT thread. Here we do our heavy stuff
bool ASyncBufferFun_NRTStage(World* world, void* inUserData)
{
    BufferFunTimeCmdData* data = (BufferFunTimeCmdData*) inUserData;
    
    SndBuf* src = World_GetNRTBuf(world, data->srcbuf);
    SndBuf* dst = World_GetNRTBuf(world, data->dstbuf);
    SCErr err = ft->fBufAlloc(dst, src->channels * data->rank, src->frames,src->samplerate);
    data->newdst = dst;
    return true;
}

//'Statge3()' happens back in the RT thread, here we swap our new buffers
//SC will send the completion message after this
bool ASyncBufferFun_RTStage(World* world, void* inUserData)
{
    BufferFunTimeCmdData* data = (BufferFunTimeCmdData*) inUserData;
    //Norhing will happen, unless we (a) assign the allocated data back to the main buffer pool (b?) Tell the server the buffer has changed
    //Get the RT buffer
    SndBuf* dstBuf = World_GetBuf(world, data->dstbuf);
    //Assign value to our NRT buffer pointer's value
    *dstBuf = *data->newdst;
    //Ping the server
    world->mSndBufUpdates[data->dstbuf].writes ++ ;
    return true;
}

//'Stage 4()' is back on the NRT, we don't have anything to do here. SC will send 'done' after this
bool ASyncBufferFun_FinalBit(World* world, void* inUserData)
{
    return true;
}

//Here we free any resources, including the struct we made at the start
void ASyncBufferFun_CleanUp(World* world, void* inUserData)
{
    BufferFunTimeCmdData* data = (BufferFunTimeCmdData*)inUserData;
    RTFree(world,data);
    //scsynth will take care of the completion message
}

//This is our entry point. We need to make a struct and populate it with good things
void ASyncBufferFun_Main(World *inWorld, void* inUserData, struct sc_msg_iter *msg, void *replyAddr)
{
    BufferFunTimeCmdData* data = (BufferFunTimeCmdData*)RTAlloc(inWorld, sizeof(BufferFunTimeCmdData));
    
    
//    size_t srcFrameCount = srcBuf->frames;
//    size_t srcChanCount = srcBuf->channels;
    
    data->srcbuf =  msg->geti();
    data->dstbuf = msg->geti();
    data->rank   =  msg->geti();
    
    bool ok = true;
    
    if(data->srcbuf < 0 )
    {
        Print("No source buffer");
        ok = false;
    }
    
    if(data->dstbuf < 0 )
    {
        Print("No dst buffer");
        ok = false;
    }
    
    if(!ok)
    {
        RTFree(inWorld,data);
        return;
    }
    
    
    
//    how to pass a string argument: [WILL BE USEFUL FOR WINDOW FUNCTIONS?]
//    const char *name = msg->gets(); // get the string argument
//    if (name) {
//        data->name = (char*)RTAlloc(inWorld, strlen(name)+1); // allocate space, free it in cmdCleanup.
//        strcpy(data->name, name); // copy the string
//    }
    
  //Deal with completion message
    size_t completionMsgSize = msg->getbsize();
    char*  completionMsgString = 0;
    if(completionMsgSize)
    {
       //allocate string
        completionMsgString = (char*)RTAlloc(inWorld,sizeof(completionMsgSize));
        msg->getb(completionMsgString,completionMsgSize);
    }
    
    //Now, set the wheels in motion
    DoAsynchronousCommand(inWorld,replyAddr,"AsyncBufMatch",
                          data,
                          (AsyncStageFn)ASyncBufferFun_NRTStage,
                          (AsyncStageFn)ASyncBufferFun_RTStage,
                          (AsyncStageFn)ASyncBufferFun_FinalBit,
                          ASyncBufferFun_CleanUp,
                          completionMsgSize, completionMsgString);
}

PluginLoad(BufferFunTime) {
    ft = inTable;
    //BufGen version: all in the NRT thread
    DefineBufGen("BufMatch", BufferMatch);
    //ASync version: swaps between NRT and RT threads
    DefinePlugInCmd("AsyncBufMatch", ASyncBufferFun_Main, nullptr);
    
}
