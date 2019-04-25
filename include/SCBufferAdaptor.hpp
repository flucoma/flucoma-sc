#pragma once

#include <SC_PlugIn.h>
#include <boost/align/aligned_alloc.hpp>
#include <cctype>
#include <data/FluidTensor.hpp>
#include <clients/common/BufferAdaptor.hpp>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>


namespace fluid
{
namespace client
{
/**
 A descendent of SndBuf that will populate itself
 from the NRT mirror buffers given a world and a bufnum
 **/
struct NRTBuf {
  NRTBuf(SndBuf *b)
      : mBuffer(b)
  {
  }
  NRTBuf(World *world, uint32 bufnum, bool rt = false)
      : NRTBuf(rt ? World_GetBuf(world, bufnum)
                  : World_GetNRTBuf(world, bufnum))
  {
    if (mBuffer && !static_cast<bool>(mBuffer->samplerate))
      mBuffer->samplerate = world->mFullRate.mSampleRate;
  }

protected:
  SndBuf *mBuffer;
};

/**
 A combination of SndBuf and client::BufferAdaptor (which, in turn, exposes
 FluidTensorView<float,2>), for simple transfer of data

 Given a World* and a buffer number, this will populate its SndBuf stuff
 from the NRT mirror buffers, and create a FluidTensorView wrapper of
 appropriate dimensions.

 The SndBuf can then be 'transferred' back to the RT buffers once we're done
 with it, and SC notified of the update. (In the context of SequencedCommands,
 in which this is meant to be used, this would happen at Stage3() on the
 real-time thread)

 nSamps = rows
 nChans = columns
 **/
class SCBufferAdaptor : public NRTBuf, public client::BufferAdaptor
{
public:
//  SCBufferAdaptor()               = delete;
  SCBufferAdaptor(const SCBufferAdaptor &) = delete;
  SCBufferAdaptor& operator=(const SCBufferAdaptor &) = delete;

  SCBufferAdaptor(SCBufferAdaptor&&) = default;
  SCBufferAdaptor& operator=(SCBufferAdaptor&&) = default;


  SCBufferAdaptor(intptr_t bufnum,World *world, bool rt = false)
      : NRTBuf(world, static_cast<uint32>(bufnum), rt)
      , mBufnum(bufnum)
      , mWorld(world)
  {
  }
  

  SCBufferAdaptor() = default;

  ~SCBufferAdaptor(){ cleanUp(); }

  void assignToRT(World *rtWorld)
  {
    SndBuf *rtBuf = World_GetBuf(rtWorld, static_cast<uint32>(mBufnum));
    *rtBuf        = *mBuffer;
    rtWorld->mSndBufUpdates[mBufnum].writes++;
  }

  void cleanUp()
  {
    if (mOldData)
    {
      boost::alignment::aligned_free(mOldData);
      mOldData = nullptr;
    } 
  }

  // No locks in (vanilla) SC, so no-ops for these
  bool acquire() override { return true; }
  void release() override {}

  // Validity is based on whether this buffer is within the range the server
  // knows about
  bool valid() const override
  {
    return (mBuffer && mBufnum >= 0 && mBufnum < mWorld->mNumSndBufs);
  }
  
  bool exists() const override
  {
    return true; 
  }

  FluidTensorView<float, 1> samps(size_t channel, size_t rankIdx = 0) override
  {
    FluidTensorView<float, 2> v{mBuffer->data, 0,
                                static_cast<size_t>(mBuffer->frames),
                                static_cast<size_t>(mBuffer->channels)};

    return v.col(rankIdx + channel * mRank);
  }

  // Return a 2D chunk
  FluidTensorView<float, 1> samps(size_t offset, size_t nframes,
                                  size_t chanoffset) override
  {
    FluidTensorView<float, 2> v{mBuffer->data, 0,
                                static_cast<size_t>(mBuffer->frames),
                                static_cast<size_t>(mBuffer->channels)};

    return v(fluid::Slice(offset, nframes), fluid::Slice(chanoffset, 1)).col(0);
  }

  size_t numFrames() const override
  {
    return valid() ? static_cast<size_t>(this->mBuffer->frames) : 0u;
  }

  size_t numChans() const override
  {
    return valid() ? static_cast<size_t>(this->mBuffer->channels) / mRank : 0u;
  }

  size_t rank() const override { return valid() ? mRank : 0u; }

  double sampleRate() const override { return valid() ? mBuffer->samplerate : 0; }

  void resize(size_t frames, size_t channels, size_t rank, double sampleRate) override
  {
    SndBuf *thisThing = mBuffer;
    mOldData          = thisThing->data;
    mRank             = rank;
    mWorld->ft->fBufAlloc(mBuffer, static_cast<int>(channels * rank), static_cast<int>(frames), sampleRate);
  }

  intptr_t bufnum() { return mBufnum; }
  void realTime(bool rt) { mRealTime = rt;  }

protected:

  bool  mRealTime{false};
  float *mOldData{0};
  intptr_t   mBufnum;
  World *mWorld;
  size_t mRank{1};
};

std::ostream& operator <<(std::ostream& os, SCBufferAdaptor& b)
{
  return os << b.bufnum(); 
}

} // namespace client
} // namespace fluid

