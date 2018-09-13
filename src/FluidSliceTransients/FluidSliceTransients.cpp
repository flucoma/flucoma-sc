
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/TransientSlice.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace segmentation{
  class FluidSliceTransients: public SCUnit
  {
    using audio_client       = TransientsSlice<double, float>;
    using AudioSignalWrapper = audio_client::AudioSignal;
    using SignalWrapper      = audio_client::Signal<float>;
    
    //  using SignalPointer = std::unique_ptr<signal_wrapper>;
  public:
    FluidSliceTransients()
    {
     m_client =  new audio_client(65536);
      setParams(true);
      bool isOK;
      std::string feedback;
      
      std::tie(isOK, feedback) = m_client->sanityCheck();
      if(!isOK)
      {
        Print("FluidSliceTransients Error: %s",feedback.c_str());
        return;
      }
      
      
      m_client->set_host_buffer_size(bufferSize());
      m_client->reset();
      
      //Work out what signals we need. For now keep it simple:
      //in 0 => only audio
      //out 0 => only audio
      input_signals[0] =  new AudioSignalWrapper();
      output_signals[0] = new AudioSignalWrapper();
      
      mCalcFunc = make_calc_function<FluidSliceTransients,&FluidSliceTransients::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }
    
    ~FluidSliceTransients()
    {
      delete input_signals[0];
      delete output_signals[0];
      delete  m_client;
    }
    
  private:
    
    void setParams(bool instantiation)
    {
      assert(m_client);
      for(size_t i = 0; i < m_client->getParams().size(); ++i)
      {
        parameter::Instance& p = m_client->getParams()[i];
        
        if(!instantiation && p.getDescriptor().instantiation())
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
      m_client->do_process(std::begin(input_signals),std::end(input_signals),std::begin(output_signals), std::end(output_signals),numsamples,1,1);
    }
    
    audio_client* m_client;
    SignalWrapper* input_signals[1];
    SignalWrapper*  output_signals[1];
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::segmentation::FluidSliceTransients>(ft, "FluidSliceTransients");
}
