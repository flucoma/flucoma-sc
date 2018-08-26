// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/GainClient.hpp"

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;
using fluid::audio::GainAudioClient;


static InterfaceTable *ft;

using client_type = GainAudioClient<double, float>;
using signal_type = client_type::signal_type;
using audio_signal= client_type::audio_signal;
using scalar_signal = client_type::scalar_signal;

class FDGain: public SCUnit
{
public:
    FDGain()
    {
        //Same calc function, all day, every day
        
        //Get the chunk size, as we need that set buffers
        const float chunk_size = in0(1);
        
        //Oh NO! Heap allocation! Make client object
        if(chunk_size)
            m_client = new client_type(chunk_size,chunk_size);
        else
            m_client = new client_type(1024,1024);
        
        m_client->set_host_buffer_size(bufferSize());
        m_client->reset();
                
        //Work out what signals we need. For now keep it simple:
        //in 0 => only audio
        //out 0 => only audio
        //in 1 => a or k 
        input_signals[0] =  new audio_signal();
        output_signals[0] = new audio_signal();
        
        if(isAudioRateIn(2))
            input_signals[1] = new audio_signal();
        else
            input_signals[1] = new scalar_signal();
        
        
//        set_calc_function<FDGain,&FDGain::next>();
        mCalcFunc = make_calc_function<FDGain,&FDGain::next>();
        
//
        Unit* unit = this;
        ClearUnitOutputs(unit,1);
        
    }
    
    ~FDGain()
    {
        delete input_signals[0];
        delete input_signals[1];
        delete output_signals[0];
        delete m_client;
    }
    
private:
    
    void next(int numsamples)
    {
        //TODO: Remove const_cast code smell by making input_signal type for const 
        input_signals[0]->set(const_cast<float*>(in(0)), in0(0));
        input_signals[1]->set(const_cast<float*>(in(2)), in0(2));
        output_signals[0]->set(out(0), out0(0));
        
        m_client->do_process(input_signals,output_signals,numsamples,2,1);
    }
    
    client_type* m_client;
    signal_type* input_signals[2];
    signal_type* output_signals[1];
};

PluginLoad(BoringMixer2UGens) {
    ft = inTable;
    registerUnit<FDGain>(ft, "FDGain");
}




