
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/BaseSTFTClient.hpp"

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;
using fluid::audio::BaseSTFTClient;


static InterfaceTable *ft;

using audio_client = BaseSTFTClient<double, float>;
using audio_signal_wrapper = audio_client::AudioSignal;
using scalar_signal_wrapper = audio_client::ScalarSignal;
using signal_wrapper = audio_client::Signal<float>;

class FDSTFTPass: public SCUnit
{
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
        //This will do argument checking
        m_client = new audio_client(window_size, hop_size, fft_size);
       
        //and we can post strings back about the argument checking
        //Params get checked and adjusted during construction, we can pass on warning messages:
        std::istringstream param_feedback(m_client->getFeedbackString());
        std::string message;
        
        while(std::getline(param_feedback, message, '\n'))
            Print(message.c_str());
        
        m_client->set_host_buffer_size(bufferSize());
        m_client->reset();
                
        //Work out what signals we need. For now keep it simple:
        //in 0 => only audio
        //out 0 => only audio
        //out 1 => only audio (and only internal, used for OLA gain compensation)
        input_signals[0] =  new audio_signal_wrapper();
        output_signals[0] = new audio_signal_wrapper();
        output_signals[1] = new audio_signal_wrapper();
        
        m_normalise = new float[bufferSize()];
        
  
//        set_calc_function<FDGain,&FDGain::next>();
        mCalcFunc = make_calc_function<FDSTFTPass,&FDSTFTPass::next>();
        
        Unit* unit = this;
        ClearUnitOutputs(unit,1);
        
    }
    
    ~FDSTFTPass()
    {
        delete[] m_normalise;
        delete input_signals[0];
        
        delete output_signals[0];
        delete output_signals[1];

        delete m_client;
    }
    
private:
    
    void next(int numsamples)
    {
        //TODO: Remove const_cast code smell by making input_signal type for const 
        const float* input = in(0);
        const float inscalar = in0(0);
        float* output = out(0);
        
        input_signals[0]->set(const_cast<float*>(input), inscalar);

        output_signals[0]->set(out(0), out0(0));
        output_signals[1]->set(m_normalise,0);
        
        m_client->do_process(input_signals,output_signals,numsamples,1,2);
        
        //We need to normalise *after* the overlap add, because of the way we do it
        std::transform(output, output + numsamples, m_normalise, output, std::divides<double>());
        
    }
    
    audio_client* m_client;
    signal_wrapper* input_signals[1];
    signal_wrapper* output_signals[2];
    float* m_normalise;
};

PluginLoad(BoringMixer2UGens) {
    ft = inTable;
    registerUnit<FDSTFTPass>(ft, "FDSTFTPass");
}




