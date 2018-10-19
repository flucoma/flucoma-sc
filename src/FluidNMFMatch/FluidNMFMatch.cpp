
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "fdNRTBase.hpp"
#include "clients/rt/NMFMatch.hpp"



static InterfaceTable *ft;
namespace fluid {
namespace nmf{
  class FDNMFMatch: public SCUnit
  {
    using Client             = NMFMatch<double,float>;
    using AudioSignalWrapper = Client::AudioSignal;
    using SignalWrapper      = Client::Signal<float>;
    using SignalPointer      = std::unique_ptr<SignalWrapper>;
    using ClientPointer      = std::unique_ptr<Client>;
    template <size_t N>
    using SignalArray        = std::array<SignalPointer,N>;
    using SignalVector       = std::vector<SignalPointer>;
    
  public:
    FDNMFMatch()
    {
      //Order of args
      //psize hszie pthresh hthresh   Window size, Hop size, FFT Size
   
      mClient =  ClientPointer(new Client(65536));
      
      setParams(true);
      
      bool isOK;
      std::string feedback;
      
      std::tie(isOK, feedback) = mClient->sanityCheck();
      if(!isOK)
      {
        std::cout << "FluidNMFMatch Error: " << feedback << '\n';
        
        mCalcFunc = ClearUnitOutputs;
        
        return;
      }
      
      mRank = parameter::lookupParam("rank", mClient->getParams()).getLong();
      
      
      mClient->set_host_buffer_size(bufferSize());
      mClient->reset();
      
     inputSignals[0] =  SignalPointer(new AudioSignalWrapper());
      
      outputSignals.resize(mRank);
      for(size_t i = 0; i < mRank; ++i)
        outputSignals[i].reset(new Client::ScalarSignal());
      
      mCalcFunc = make_calc_function<FDNMFMatch,&FDNMFMatch::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }
    
    ~FDNMFMatch() {}
    
  private:
    
    void setParams(bool instantiation)
    {
      assert(mClient);
      for(size_t i = 0; i < mClient->getParams().size(); ++i)
      {
        parameter::Instance& p = mClient->getParams()[i];
        if(!instantiation && p.getDescriptor().instantiation())
          continue;
        switch(p.getDescriptor().getType())
        {
          case parameter::Type::Long:
            p.setLong(in0(i+1));
            p.checkRange();
            break;
          case parameter::Type::Float:
          {
            p.setFloat(in0(i+1));
            p.checkRange();
          }
            break;
          case parameter::Type::Buffer:
          {
            long bufnum = static_cast<long>(in0(i+1));
            sc::RTBufferView* currentBuf = static_cast<sc::RTBufferView*>(p.getBuffer());
            
            
            if(bufnum >= 0 && (currentBuf? (currentBuf->bufnum() != bufnum) : true)){
              sc::RTBufferView* buf = new sc::RTBufferView(mWorld,bufnum);
              p.setBuffer(buf);
            }
            break;
          }
          default:
            break;
        }
      }
    }
    
    void next(int numsamples)
    {
      setParams(false);
      const float* input = zin(0);
      const float inscalar = in0(0);
      inputSignals[0]->set(const_cast<float*>(input), inscalar);
      for(size_t i = 0; i < mRank; ++i)
        outputSignals[i]->set(out(i),out0(i));
      
      mClient->do_process_noOLA(inputSignals.begin(),inputSignals.end(), outputSignals.begin(), outputSignals.end(), mWorld->mFullRate.mBufLength ,1,mRank);
      for(size_t i = 0; i < mRank; ++i)
        out0(i) = outputSignals[i]->next();
    }
    


    size_t mRank;
    ClientPointer mClient;
    SignalArray<1> inputSignals;
    SignalVector   outputSignals;
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::nmf::FDNMFMatch>(ft, "FluidNMFMatch");
}




