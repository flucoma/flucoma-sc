
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/TransientClient.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace wrapper{
  class FluidTransients: public SCUnit
  {
    using AudioSignalWrapper = client::TransientsClient<double, float>::AudioSignal;
    using SignalWrapper      = client::TransientsClient<double, float>::Signal<float>;

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
      mClient =  new client::TransientsClient<double,float>(65536);
      setParams(true);

//      mClient->geParams()[0].setLong(pfilter_size);
//      mClient->geParams()[1].setLong(hfilter_size);
//      mClient->geParams()[2].setLong(window_size);
//      mClient->geParams()[3].setLong(hop_size);
//      mClient->geParams()[4].setLong(fft_size);

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
      //in 0 => only audio
      //out 0 => only audio
      inputSignals[0] =  new AudioSignalWrapper();
      outputSignals[0] = new AudioSignalWrapper();
      outputSignals[1] = new AudioSignalWrapper();

      mCalcFunc = make_calc_function<FluidTransients,&FluidTransients::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }

    ~FluidTransients()
    {
      delete inputSignals[0];
      delete outputSignals[0];
      delete outputSignals[1];
      delete  mClient;
    }
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
        case client::Type::kFloat:
          p.setFloat(in0(i + 1));
          p.checkRange();
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
      mClient->doProcess(std::begin(inputSignals),std::end(inputSignals),std::begin(outputSignals), std::end(outputSignals),numsamples,1,2);
    }

    client::TransientsClient<double, float> *mClient;
    SignalWrapper *inputSignals[1];
    SignalWrapper *outputSignals[2];
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::wrapper::FluidTransients>(ft, "FluidTransients");
}
