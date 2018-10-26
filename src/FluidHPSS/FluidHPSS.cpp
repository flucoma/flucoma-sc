
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/HPSSClient.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace wrapper{
  class FDRTHPSS: public SCUnit
  {
    using AudioSignalWrapper = client::HPSSClient<double, float>::AudioSignal;
    using SignalWrapper      = client::HPSSClient<double, float>::Signal<float>;
    using SignalPointer      = std::unique_ptr<SignalWrapper>;
    using ClientPointer      = std::unique_ptr<client::HPSSClient<double,float>>;
    template <size_t N>
    using SignalArray        = std::array<SignalPointer,N>;
  public:
    FDRTHPSS()
    {
      //Order of args
      //psize hszie pthresh hthresh   Window size, Hop size, FFT Size
   
      //Oh NO! Heap allocation! Make client object
      mClient =  ClientPointer(new client::HPSSClient<double,float>(65536));
      
      setParams(true);
      
      bool isOK;
      std::string feedback;
      
      std::tie(isOK, feedback) = mClient->sanityCheck();
      if(!isOK)
      {
        Print("fdRTHPSS Error: %s",feedback.c_str());
        return;
      }
      
      
      mClient->setHostBufferSize(bufferSize());
      mClient->reset();
      
      //Work out what signals we need. For now keep it simple:
      inputSignals[0] =  SignalPointer(new AudioSignalWrapper());
      outputSignals[0] = SignalPointer(new AudioSignalWrapper());
      outputSignals[1] = SignalPointer(new AudioSignalWrapper());
      outputSignals[2] = SignalPointer(new AudioSignalWrapper());
      
      mCalcFunc = make_calc_function<FDRTHPSS,&FDRTHPSS::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }
    
    ~FDRTHPSS() {}
    
  private:
    
    void setParams(bool instantiation)
    {
      assert(mClient);
      for(size_t i = 0; i < mClient->getParams().size(); ++i)
      {
        client::Instance& p = mClient->getParams()[i];
        if(!instantiation && p.getDescriptor().instantiation())
          continue;
        switch(p.getDescriptor().getType())
        {
        case client::Type::kLong:
          p.setLong(in0(i + 1));
          p.checkRange();
          break;
        case client::Type::kFloat: {

          // We need to constrain threshold (normalised) frequency pairs at
          // runtime.
          std::string attrname = p.getDescriptor().getName();
          auto constraint = paramConstraints.find(attrname);

          if (constraint != paramConstraints.end()) {
            double limit = client::lookupParam(constraint->second.param,
                                                  mClient->getParams())
                               .getFloat();

            if (!constraint->second.condition(in0(i + 1), limit)) {
              return;
            }
          }

          p.setFloat(in0(i + 1));
          p.checkRange();
          }
            break;
          case client::Type::kBuffer:
            //            p.setBuffer( in0(i+1));
            break;
          default:
            break;
        }
      }
    }
    
    void next(int numsamples)
    {
      setParams(false);
      const float* input = in(0);
      const float inscalar = in0(0);
      inputSignals[0]->set(const_cast<float*>(input), inscalar);
      outputSignals[0]->set(out(0), out0(0));
      outputSignals[1]->set(out(1), out0(1));
      outputSignals[2]->set(out(2), out0(2));
      mClient->doProcess(std::begin(inputSignals),std::end(inputSignals),std::begin(outputSignals), std::end(outputSignals),numsamples,1,3);
    }
    
    struct Constraint{
      std::string param;
      std::function<bool(double, double)> condition;
    };
    
    std::map<std::string, Constraint> paramConstraints{
      {"ptf1",{"ptf2", std::less<double>()}},
      {"htf1",{"htf2", std::less<double>()}},
      {"ptf2",{"ptf1", std::greater<double>()}},
      {"htf2",{"htf1", std::greater<double>()}}
    };
    

    ClientPointer mClient;
    SignalArray<1> inputSignals;
    SignalArray<3> outputSignals;
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::wrapper::FDRTHPSS>(ft, "FluidHPSS");
}




