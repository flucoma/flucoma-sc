
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.hpp"
#include "data/FluidTensor.hpp"
#include "clients/rt/TransientSlice.hpp"

static InterfaceTable *ft;
namespace fluid {
namespace segmentation{
  class FluidSliceTransients: public SCUnit
  {
    using audio_client       = client::TransientsSlice<double, float>;
    using AudioSignalWrapper = audio_client::AudioSignal;
    using SignalWrapper      = audio_client::Signal<float>;

    //  using SignalPointer = std::unique_ptr<signal_wrapper>;
  public:
    FluidSliceTransients()
    {
     mClient =  new audio_client(65536);
      setParams(true);
      bool isOK;
      std::string feedback;

      std::tie(isOK, feedback) = mClient->sanityCheck();
      if(!isOK)
      {
        Print("FluidSliceTransients Error: %s",feedback.c_str());
        return;
      }


      mClient->setHostBufferSize(bufferSize());
      mClient->reset();

      //Work out what signals we need. For now keep it simple:
      //in 0 => only audio
      //out 0 => only audio
      inputSignals[0] =  new AudioSignalWrapper();
      outputSignals[0] = new AudioSignalWrapper();

      mCalcFunc = make_calc_function<FluidSliceTransients,&FluidSliceTransients::next>();
      Unit* unit = this;
      ClearUnitOutputs(unit,1);
    }

    ~FluidSliceTransients()
    {
      delete inputSignals[0];
      delete outputSignals[0];
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
      mClient->doProcess(std::begin(inputSignals),std::end(inputSignals),std::begin(outputSignals), std::end(outputSignals),numsamples,1,1);
    }

    audio_client *mClient;
    SignalWrapper *inputSignals[1];
    SignalWrapper *outputSignals[1];
  };
}
}

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  registerUnit<fluid::segmentation::FluidSliceTransients>(ft, "FluidTransientSlice");
}
