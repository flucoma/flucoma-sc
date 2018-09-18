
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/HPSSClient.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace hpss{
  class FDRTHPSS: public SCUnit
  {
    using AudioSignalWrapper = hpss::HPSSClient<double, float>::AudioSignal;
    using SignalWrapper      = hpss::HPSSClient<double, float>::Signal<float>;
    using SignalPointer      = std::unique_ptr<SignalWrapper>;
    using ClientPointer      = std::unique_ptr<hpss::HPSSClient<double,float>>;
    template <size_t N>
    using SignalArray        = std::array<SignalPointer,N>;
  public:
    FDRTHPSS()
    {
      //Order of args
      //psize hszie pthresh hthresh   Window size, Hop size, FFT Size
   
      //Oh NO! Heap allocation! Make client object
      mClient =  ClientPointer(new hpss::HPSSClient<double,float>(65536));
      
      setParams(true);
      
      bool isOK;
      std::string feedback;
      
      std::tie(isOK, feedback) = mClient->sanityCheck();
      if(!isOK)
      {
        Print("fdRTHPSS Error: %s",feedback.c_str());
        return;
      }
      
      
      mClient->set_host_buffer_size(bufferSize());
      mClient->reset();
      
      //Work out what signals we need. For now keep it simple:
      input_signals[0] =  SignalPointer(new AudioSignalWrapper());
      output_signals[0] = SignalPointer(new AudioSignalWrapper());
      output_signals[1] = SignalPointer(new AudioSignalWrapper());
      output_signals[2] = SignalPointer(new AudioSignalWrapper());
      
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
            
            //We need to constrain threshold (normalised) frequency pairs at runtime.
            std::string attrname  = p.getDescriptor().getName();
            auto constraint = paramConstraints.find(attrname);
            
            if(constraint != paramConstraints.end())
            {
              double limit = parameter::lookupParam(constraint->second.param, mClient->getParams()).getFloat();
              
              if(!constraint->second.condition(in0(i+1),limit))
              {
                return;
              }
            }
            
            p.setFloat(in0(i+1));
            p.checkRange();
          }
            break;
          case parameter::Type::Buffer:
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
      input_signals[0]->set(const_cast<float*>(input), inscalar);
      output_signals[0]->set(out(0), out0(0));
      output_signals[1]->set(out(1), out0(1));
      output_signals[2]->set(out(2), out0(2));
      mClient->do_process(std::begin(input_signals),std::end(input_signals),std::begin(output_signals), std::end(output_signals),numsamples,1,3);
    }
    
    struct Constraint{
      std::string param;
      std::function<bool(double, double)> condition;
    };
    
    std::map<std::string, Constraint> paramConstraints{
      {"ptf1",{"pthreshf2", std::less<double>()}},
      {"htf1",{"htf2", std::less<double>()}},
      {"ptf2",{"ptf1", std::greater<double>()}},
      {"htf2",{"htf1", std::greater<double>()}}
    };
    

    ClientPointer mClient;
    SignalArray<1> input_signals;
    SignalArray<3> output_signals;
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::hpss::FDRTHPSS>(ft, "FluidHPSS");
}




