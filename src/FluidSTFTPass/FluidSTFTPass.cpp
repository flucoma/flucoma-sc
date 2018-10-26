
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/BaseSTFTClient.hpp"

static InterfaceTable *ft;
namespace fluid {
  namespace stft{


class FDSTFTPass: public SCUnit
{
  using AudioSignalWrapper = client::BaseSTFTClient<double, float>::AudioSignal;
  using SignalWrapper      = client::BaseSTFTClient<double, float>::Signal<float>;

//  using SignalPointer = std::unique_ptr<signal_wrapper>;
public:
    FDSTFTPass()
  {
        //Order of args
        //Window size, Hop size, FFT Size

        //Get the window size
        const float windowSize = in0(1);
        const float hopSize = in0(2);
        const float fftSize = in0(3);

        //Oh NO! Heap allocation! Make client object
        mClient =  new client::BaseSTFTClient<double,float>(65536);
        mClient->getParams()[0].setLong(windowSize);
        mClient->getParams()[1].setLong(hopSize);
        mClient->getParams()[2].setLong(fftSize);

        bool isOK;
        std::string feedback;

        std::tie(isOK, feedback) = mClient->sanityCheck();
        if(!isOK)
        {
          Print("fdSTFTPass Error: %s",feedback.c_str());
          return;
        }


        mClient->setHostBufferSize(bufferSize());
        mClient->reset();

        //Work out what signals we need. For now keep it simple:
        //in 0 => only audio
        //out 0 => only audio
        inputSignals[0] =  new AudioSignalWrapper();
        outputSignals[0] = new AudioSignalWrapper();

        mCalcFunc = make_calc_function<FDSTFTPass,&FDSTFTPass::next>();
        Unit* unit = this;
        ClearUnitOutputs(unit,1);
    }

    ~FDSTFTPass()
    {
        delete inputSignals[0];
        delete outputSignals[0];
        delete  mClient;
    }

private:
    void next(int numsamples)
    {
        const float* input = in(0);
        const float inscalar = in0(0);
        inputSignals[0]->set(const_cast<float*>(input), inscalar);
        outputSignals[0]->set(out(0), out0(0));
        mClient->doProcess(std::begin(inputSignals),std::end(inputSignals),std::begin(outputSignals), std::end(outputSignals),numsamples,1,1);
    }

    client::BaseSTFTClient<double, float> *mClient;
    SignalWrapper *inputSignals[1];
    SignalWrapper *outputSignals[1];
};
}
}

PluginLoad(FluidSTFTUGen) {
    ft = inTable;
  registerUnit<fluid::stft::FDSTFTPass>(ft, "FluidSTFTPass");
}
