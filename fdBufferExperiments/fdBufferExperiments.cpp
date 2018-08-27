//Can I reallocate buffers on the server? Yes I can.
#include "SC_PlugIn.h"

static InterfaceTable *ft;

//Can we do buffer resizing in a BufGen. I think so.
//Goal here is to mimic the NMF case, allowing
//the dst buffer to be resized accordingly (and not need to
// precalculate the number of frames in advance lang-side
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


//typedef void (*BufGenFunc)(struct World *world, struct SndBuf *buf, struct sc_msg_iter *msg);


PluginLoad(BufferFunTime) {
    ft = inTable;
    DefineBufGen("BufMatch", BufferMatch);
}
