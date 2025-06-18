#pragma once

#include "ArgsFromClient.hpp"
#include "Meta.hpp"
#include "RealTimeBase.hpp"
#include "SCWorldAllocator.hpp"
#include <flucoma/clients/common/FluidBaseClient.hpp>
#include <SC_PlugIn.hpp>

// Real Time Processor
namespace fluid {
namespace client {  
namespace impl {
  
template <typename Client, class Wrapper>
class RealTime : public SCUnit
{
  
  using Delegate = impl::RealTimeBase<Client,Wrapper>;
  using Params = typename Client::ParamSetType;
  
public:

  // static index ControlOffset(Unit* unit) { return Delegate::ControlOffset(unit); }
  // static index ControlSize(Unit* unit)   { return Delegate::ControlSize(unit); }

  static index ControlOffset(Unit* unit) { return unit->mSpecialIndex + 1; }
  static index ControlSize(Unit* unit) 
  { 
    return static_cast<index>(unit->mNumInputs) 
                                - unit->mSpecialIndex 
                                - 1  
                                - (IsModel_t<Client>::value ? 1 : 0); 
  }

  static void setup(InterfaceTable* ft, const char* name)
  {
    
    registerUnit<RealTime>(ft,name);
    ft->fDefineUnitCmd(name, "latency", doLatency);
  }

  static void doLatency(Unit* unit, sc_msg_iter*)
  {
    float l[]{
         static_cast<float>(static_cast<RealTime*>(unit)->mClient.latency())
    };
    auto ft = Wrapper::getInterfaceTable();

    std::stringstream ss;
    ss << '/' << Wrapper::getName() << "_latency";
    // std::cout << ss.str() << ": " << l[0] << std::endl;
    ft->fSendNodeReply(&unit->mParent->mNode, -1, ss.str().c_str(), 1, l);
  }

  RealTime()
    :
      mSCAlloc{mWorld, Wrapper::getInterfaceTable()},
      mAlloc{foonathan::memory::make_allocator_reference(mSCAlloc)},
      mContext{fullBufferSize(), mAlloc},
      mControls{mInBuf + ControlOffset(this),ControlSize(this)},
      mParams{Client::getParameterDescriptors(), mAlloc},
      mClient{Wrapper::setParams(this, mParams, mControls, mAlloc,true), mContext}
  {
    init();
  }

  void init()
  {
    mDelegate.init(*this,mClient,mControls,mAlloc);
    mCalcFunc = make_calc_function<RealTime, &RealTime::next>();
    Wrapper::getInterfaceTable()->fClearUnitOutputs(this, 1);

   
  }

  void next(int)
  {
    mControls.reset(mInBuf + ControlOffset(this));
    mDelegate.next(*this,mClient,mParams,mControls, mAlloc);
  }
private:
  SCRawAllocator mSCAlloc;
  Allocator mAlloc;
  FluidContext mContext; 
  Delegate mDelegate;
  FloatControlsIter   mControls;
  Params mParams;
  Client mClient;
  Wrapper* mWrapper{static_cast<Wrapper*>(this)};
};

}
}
}
