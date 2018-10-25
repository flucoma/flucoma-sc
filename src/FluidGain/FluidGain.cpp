// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/GainClient.hpp"
#include "clients/common/FluidParams.hpp"


static InterfaceTable *ft;
namespace fluid {
class FDGain: public SCUnit
{
  using AudioClient = fluid::audio::GainAudioClient<double, float>;
  using ClientPointer = std::unique_ptr<AudioClient>;
  using SignalWrapper = AudioClient::Signal;
  using AudioSignal= AudioClient::AudioSignal;
  using ControlSignal = AudioClient::ScalarSignal;
  using SignalPointer = std::unique_ptr<SignalWrapper>;
  template <size_t N>
  using SignalArray = std::array<SignalPointer, N>;
public:
  FDGain()
  {
    mClient = ClientPointer(new AudioClient(65536));
    
    std::vector<parameter::Instance>& params =  mClient->getParams();
    
    parameter::lookupParam("winsize", params).setLong(in0(1));
    parameter::lookupParam("hopsize", params).setLong(in0(1)); 
    
    mClient->set_host_buffer_size(bufferSize());
    mClient->reset();
    
    inputSignals[0] =  SignalPointer(new AudioSignal());
    outputSignals[0] = SignalPointer(new AudioSignal());
    
    if(isAudioRateIn(2))
      inputSignals[1] = SignalPointer(new AudioSignal());
    else
      inputSignals[1] = SignalPointer(new ControlSignal());
    
    mCalcFunc = make_calc_function<FDGain,&FDGain::next>();
    Unit* unit = this;
    ClearUnitOutputs(unit,1);
  }
  
  ~FDGain() {}
private:
  void next(int numsamples)
  {
    //TODO: Remove const_cast code smell by making input_signal type for const
    inputSignals[0]->set(const_cast<float*>(in(0)), in0(0));
    inputSignals[1]->set(const_cast<float*>(in(2)), in0(2));
    outputSignals[0]->set(out(0), out0(0));
    mClient->do_process(inputSignals.begin(),inputSignals.end(),outputSignals.begin(),outputSignals.end(),numsamples,2,1);
  }
  
  ClientPointer mClient;
  SignalArray<2> inputSignals;
  SignalArray<1> outputSignals;
};
}
PluginLoad(BoringMixer2UGens) {
  ft = inTable;
  registerUnit<fluid::FDGain>(ft, "FluidGain");
}
