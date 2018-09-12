
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/TransientClient.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace stn{
  class FluidTransients: public SCUnit
  {
    using AudioSignalWrapper = stn::TransientsClient<double, float>::AudioSignal;
    using SignalWrapper      = stn::TransientsClient<double, float>::Signal<float>;
    
    //  using SignalPointer = std::unique_ptr<signal_wrapper>;
  public:
    FluidTransients()
    {
      //Order of args
      //Window size, Hop size, FFT Size
      
      //Get the window size
      
      
//      const float hfilter_size = in0(1);
//      const float pfilter_size = in0(2);
//      const float window_size = in0(3);
//      const float hop_size = in0(4);
//      const float fft_size = in0(5);
//
      
      //Oh NO! Heap allocation! Make client object
      m_client =  new stn::TransientsClient<double,float>(65536);
      setParams(true);
      
//      m_client->getParams()[0].setLong(pfilter_size);
//      m_client->getParams()[1].setLong(hfilter_size);
//      m_client->getParams()[2].setLong(window_size);
//      m_client->getParams()[3].setLong(hop_size);
//      m_client->getParams()[4].setLong(fft_size);
      
      bool isOK;
      std::string feedback;
      
      std::tie(isOK, feedback) = m_client->sanityCheck();
      if(!isOK)
      {
        Print("fdRTHPSS Error: %s",feedback.c_str());
        return;
      }
      
      
      m_client->set_host_buffer_size(bufferSize());
      m_client->reset();
      
      //Work out what signals we need. For now keep it simple:
      //in 0 => only audio
      //out 0 => only audio
      input_signals[0] =  new AudioSignalWrapper();
      output_signals[0] = new AudioSignalWrapper();
      output_signals[1] = new AudioSignalWrapper();
      
      mCalcFunc = make_calc_function<FluidTransients,&FluidTransients::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }
    
    ~FluidTransients()
    {
      delete input_signals[0];
      delete output_signals[0];
      delete output_signals[1];
      delete  m_client;
    }
    
  private:
    
    void setParams(bool instantiation)
    {
      assert(m_client);
      for(size_t i = 0; i < m_client->getParams().size(); ++i)
      {
        parameter::Instance& p = m_client->getParams()[i];
        
        if(!instantiation && p.getDescriptor().instatiation())
          continue;
        
        switch(p.getDescriptor().getType())
        {
          case parameter::Type::Long:
            p.setLong(in0(i+1));
            p.checkRange();
            break;
          case parameter::Type::Float:
            p.setFloat(in0(i+1));
            p.checkRange();
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
      m_client->do_process(std::begin(input_signals),std::end(input_signals),std::begin(output_signals), std::end(output_signals),numsamples,1,2);
    }
    
    stn::TransientsClient<double, float>* m_client;
    SignalWrapper* input_signals[1];
    SignalWrapper*  output_signals[2];
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::stn::FluidTransients>(ft, "FluidTransients");
}
