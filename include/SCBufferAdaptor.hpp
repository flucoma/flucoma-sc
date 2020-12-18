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

#include <boost/align/aligned_alloc.hpp>
#include <clients/common/BufferAdaptor.hpp>
#include <data/FluidTensor.hpp>
#include <SC_Errors.h>
#include <SC_PlugIn.h>
#include <cctype>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>


namespace fluid {
namespace client {
/* 
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
class SCBufferAdaptor; 
std::ostream& operator<<(std::ostream& os, SCBufferAdaptor& b);
 
class SCBufferAdaptor :public client::BufferAdaptor
{
public:
    
  SCBufferAdaptor(const SCBufferAdaptor&) = delete;
  SCBufferAdaptor& operator=(const SCBufferAdaptor&) = delete;

  SCBufferAdaptor(SCBufferAdaptor&&) = default;
  
  SCBufferAdaptor(SndBuf* buf, World* world, bool local)
      : mBuffer{buf}, mWorld{world}, mLocal{local}
  {}

  SCBufferAdaptor(index bufnum, World* world)
      : mBuffer{World_GetNRTBuf(world, static_cast<uint32>(bufnum))}, mBufnum(bufnum), mWorld(world)
  {  
      if (mBuffer && !static_cast<bool>(mBuffer->samplerate))
        mBuffer->samplerate = world->mFullRate.mSampleRate;
  }

//  ~SCBufferAdaptor() { cleanUp(); }

  void assignToRT(World* rtWorld)
  {
    if(mLocal) return;
    
    SndBuf* rtBuf = World_GetBuf(rtWorld, static_cast<uint32>(mBufnum));
    *rtBuf = *mBuffer;
    rtWorld->mSndBufUpdates[mBufnum].writes++;
  }

  void cleanUp()
  {
    boost::alignment::aligned_free(mOldData);
    mOldData = nullptr;
  }

  // No locks in (vanilla) SC, so no-ops for these
  bool acquire() const override { return true; }
  void release() const override {}

  // Validity is based on whether this buffer is within the range the server
  // knows about
  bool valid() const override
  {
    return (mLocal ? true : mBufnum >= 0 && mBufnum < asSigned(mWorld->mNumSndBufs));
  }

  bool exists() const override { return true; }

  FluidTensorView<float, 2> allFrames() override
  {
  
    FluidTensorView<float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                mBuffer->channels};
    return v.transpose();
  }

  FluidTensorView<const float, 2> allFrames() const override
  {
    FluidTensorView<const float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                mBuffer->channels};
    return v.transpose();
  }

  FluidTensorView<float, 1> samps(index channel) override
  {
    FluidTensorView<float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                mBuffer->channels};

    return v.col(channel);
  }

  FluidTensorView<float, 1> samps(index offset, index nframes,
                                  index chanoffset) override
  {
    FluidTensorView<float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                mBuffer->channels};

    return v(fluid::Slice(offset, nframes), fluid::Slice(chanoffset, 1)).col(0);
  }

  FluidTensorView<const float, 1> samps(index channel) const override
  {
    FluidTensorView<const float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                      mBuffer->channels};

    return v.col(channel);
  }

  FluidTensorView<const float, 1> samps(index offset, index nframes,
                                        index chanoffset) const override
  {
    FluidTensorView<const float, 2> v{mBuffer->data, 0, mBuffer->frames,
                                      mBuffer->channels};

    return v(fluid::Slice(offset, nframes), fluid::Slice(chanoffset, 1)).col(0);
  }


  index numFrames() const override
  {
    return valid() ? this->mBuffer->frames : 0;
  }

  index numChans() const override
  {
    return valid() ? this->mBuffer->channels : 0;
  }

  double sampleRate() const override
  {
    return valid() ? mBuffer->samplerate : 0;
  }

  std::string asString() const override { return std::to_string(bufnum()); }

  const Result resize(index frames, index channels, double sampleRate) override
  {    
    if(mLocal) // don't try and resize
    {
      if(frames > mBuffer->frames || channels > mBuffer->channels)
      {
        return {Result::Status::kError, "Local buffer must be presized adequetly, need ",
                                                        frames, " frames, ", channels, " channels." };
      }
      else return {};
    }
    
    mOldData = mBuffer->data;
    int allocResult =
        mWorld->ft->fBufAlloc(mBuffer, static_cast<int>(channels),
                              static_cast<int>(frames), sampleRate);
    
    if(mBuffer->data == mOldData) mOldData = nullptr;
    
    Result r;

    if (allocResult != kSCErr_None)
    {
      r.set(Result::Status::kError);
      r.addMessage("Resize on buffer ", bufnum(), " failed.");
    }
    return r;
  }

  index bufnum() const { return mBufnum; }
  void  realTime(bool rt) { mRealTime = rt; }

protected:
  SndBuf* mBuffer;
  bool   mRealTime{false};
  float* mOldData{nullptr};
  index  mBufnum;
  World* mWorld;
  bool   mLocal{false};
};

std::ostream& operator<<(std::ostream& os, SCBufferAdaptor& b)
{
  return os << b.bufnum();
}

} // namespace client
} // namespace fluid
