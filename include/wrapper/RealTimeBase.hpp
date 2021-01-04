#pragma once 

#include <SC_PlugIn.hpp>

namespace fluid{
namespace client{
namespace impl{
  template <typename Client, class Wrapper>
  struct RealTimeBase
  {    
    using HostVector = FluidTensorView<float, 1>;
    using Params = typename Client::ParamSetType;
    template<typename T, bool>
      struct doExpectedCount;
      
      template<typename T>
      struct doExpectedCount<T, false>
      {
        static void count(const T& d,FloatControlsIter& c,Result& status)
        {
          if(!status.ok()) return;
          
          if(c.remain())
          {
            index statedSize = d.fixedSize;
            
            if(c.remain() < statedSize)
                status =  {Result::Status::kError,"Ran out of arguments at ", d.name};
            
            //fastforward
            for(index i=0; i < statedSize; ++i) c.next();
            
          }
        }
      };

      template<typename T>
      struct doExpectedCount<T, true>
      {
        static void count(const T& d,FloatControlsIter& c,Result& status)
        {
          if(!status.ok()) return;
          
          if(c.remain())
          {
            index statedSize = 1; 
          
            if(c.remain() < statedSize)
                status =  {Result::Status::kError,"Ran out of arguments at ", d.name};
            
            //fastforward
            for(index i=0; i < statedSize; ++i) c.next();
            
          }
        }
      };
    template<size_t N, typename T>
    struct ExpectedCount{
      void operator ()(const T& descriptor,FloatControlsIter& c, Result& status)
      {
        doExpectedCount<T,IsSharedClientRef<typename T::type>::value>::count(descriptor,c,status);
      }
    };

    Result expectedSize(FloatControlsIter& controls)
    {
      if(controls.size() < Client::getParameterDescriptors().count())
      {
        return {Result::Status::kError,"Fewer parameters than exepected. Got ", controls.size(), "expect at least", Client::getParameterDescriptors().count()};
      }
      
      Result countScan;
      Client::getParameterDescriptors().template iterate<ExpectedCount>(
          std::forward<FloatControlsIter&>(controls),
          std::forward<Result&>(countScan));
      return countScan;
    }

    // static index ControlOffset(Unit* unit) { return unit->mSpecialIndex + 1; }
    // static index ControlSize(Unit* unit) { return static_cast<index>(unit->mNumInputs) - unit->mSpecialIndex - 1  -(IsModel_t<Client>::value ? 1 : 0); }
    
    void init(SCUnit& unit, Client& client, FloatControlsIter& controls)
    {
      assert(!(client.audioChannelsOut() > 0 && client.controlChannelsOut() > 0) &&"Client can't have both audio and control outputs");
      // consoltr.reset(unit.mInBuf + unit.mSpecialIndex + 1);
      client.sampleRate(unit.fullSampleRate());
      mInputConnections.reserve(asUnsigned(client.audioChannelsIn()));
      mOutputConnections.reserve(asUnsigned(client.audioChannelsOut()));
      mAudioInputs.reserve(asUnsigned(client.audioChannelsIn()));
      mOutputs.reserve(asUnsigned(
          std::max(client.audioChannelsOut(), client.controlChannelsOut())));


          Result r;
          if(!(r = expectedSize(controls)).ok())
          {
//            mCalcFunc = Wrapper::getInterfaceTable()->fClearUnitOutputs;
            std::cout
                << "ERROR: " << Wrapper::getName()
                << " wrong number of arguments."
                << r.message()
                << std::endl;
            return;
          }


      for (index i = 0; i < client.audioChannelsIn(); ++i)
      {
        mInputConnections.emplace_back(unit.isAudioRateIn(static_cast<int>(i)));
        mAudioInputs.emplace_back(nullptr, 0, 0);
      }

      for (index i = 0; i < client.audioChannelsOut(); ++i)
      {
        mOutputConnections.emplace_back(true);
        mOutputs.emplace_back(nullptr, 0, 0);
      }

      for (index i = 0; i < client.controlChannelsOut(); ++i)
      { 
        mOutputs.emplace_back(nullptr, 0, 0); 
      }
   }
    
   void next(SCUnit& unit, Client& client,Params& params,FloatControlsIter& controls)
   {   
     bool trig =  IsModel_t<Client>::value ? !mPrevTrig  && unit.in0(0) > 0 : false;
     bool shouldProcess = IsModel_t<Client>::value ? trig : true;
     mPrevTrig = trig;
     
//     if(shouldProcess)
//     {
       // controls.reset(unit.mInBuf + unit.mSpecialIndex + 1);
       Wrapper::setParams(&unit, params, controls);
       params.constrainParameterValues();
//     }
     
     for (index i = 0; i < client.audioChannelsIn(); ++i)
     {
        assert(i <= std::numeric_limits<int>::max());
       if (mInputConnections[asUnsigned(i)])
         mAudioInputs[asUnsigned(i)].reset(const_cast<float*>(unit.in(static_cast<int>(i))), 0,
                                           unit.fullBufferSize());
     }
     
     for (index i = 0; i < client.audioChannelsOut(); ++i)
     {
       assert(i <= std::numeric_limits<int>::max());
       if (mOutputConnections[asUnsigned(i)])
         mOutputs[asUnsigned(i)].reset(unit.out(static_cast<int>(i)), 0,
                                       unit.fullBufferSize());
     }
     
     for (index i = 0; i < client.controlChannelsOut(); ++i)
     {
       assert(i <= std::numeric_limits<int>::max());
       mOutputs[asUnsigned(i)].reset(unit.out(static_cast<int>(i)), 0, 1);
     }
     client.process(mAudioInputs, mOutputs, mContext);
   }  
  private:
    std::vector<bool>       mInputConnections;
    std::vector<bool>       mOutputConnections;
    std::vector<HostVector> mAudioInputs;
    std::vector<HostVector> mOutputs;
    FluidContext            mContext;
    bool                    mPrevTrig;    
  };
}
}
}
