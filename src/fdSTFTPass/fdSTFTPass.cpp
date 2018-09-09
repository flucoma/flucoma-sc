
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/BaseSTFTClient.hpp"

static InterfaceTable *ft;
namespace fluid {
  namespace stft{


class FDSTFTPass: public SCUnit
{
  using AudioSignalWrapper = audio::BaseSTFTClient<double, float>::AudioSignal;
  using SignalWrapper      = audio::BaseSTFTClient<double, float>::Signal<float>;

//  using SignalPointer = std::unique_ptr<signal_wrapper>;
public:
    FDSTFTPass()
  {
        //Order of args
        //Window size, Hop size, FFT Size
        
        //Get the window size
        const float window_size = in0(1);
        const float hop_size = in0(2);
        const float fft_size = in0(3);
        
        //Oh NO! Heap allocation! Make client object
        m_client =  new audio::BaseSTFTClient<double,float>(window_size,hop_size,fft_size);
        m_client->set_host_buffer_size(bufferSize());
        m_client->reset();
                
        //Work out what signals we need. For now keep it simple:
        //in 0 => only audio
        //out 0 => only audio
        input_signals[0] =  new AudioSignalWrapper();
        output_signals[0] = new AudioSignalWrapper();
    
        mCalcFunc = make_calc_function<FDSTFTPass,&FDSTFTPass::next>();
        Unit* unit = this;
        ClearUnitOutputs(unit,1);
    }
    
    ~FDSTFTPass()
    {
        delete input_signals[0];
        delete output_signals[0];
        delete  m_client;
    }
    
private:
    void next(int numsamples)
    {
        const float* input = in(0);
        const float inscalar = in0(0);
        input_signals[0]->set(const_cast<float*>(input), inscalar);
        output_signals[0]->set(out(0), out0(0));
        m_client->do_process(std::begin(input_signals),std::end(input_signals),std::begin(output_signals), std::end(output_signals),numsamples,1,1);
    }
    
  audio::BaseSTFTClient<double, float>* m_client;
  SignalWrapper* input_signals[1];
  SignalWrapper*  output_signals[1];
};
}
}

PluginLoad(FluidSTFTUGen) {
    ft = inTable;
  registerUnit<fluid::stft::FDSTFTPass>(ft, "FDSTFTPass");
}




