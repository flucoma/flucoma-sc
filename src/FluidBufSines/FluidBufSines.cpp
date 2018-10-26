  // FD_BufSines, an NRT buffer Sinusoidal Modelling Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "fdNRTBase.hpp"
#include "algorithms/STFT.hpp"
#include "data/FluidTensor.hpp"
#include "clients/nrt/Sines.hpp"
#include "clients/common/FluidParams.hpp"
#include "SC_PlugIn.h"
#include <unordered_set>
#include <vector>

static InterfaceTable *ft;

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;

namespace fluid {
  namespace wrapper{
    
    class BufSines: public NRTCommandBase
    {
      /*
       - srcbuf num
       – src start frame
       - src numframes
       – src start chan
       – src num chans
       – sines dst
       – residual dst
       – bandwidth (bins)
       – threshold (0-1)
       – minTrackLen (frames, 0 for no tracking)
       - magnitude weight(0-1)
       - freq weight (0-1)
       – window size
       – hop size
       – fft size
       */
    public:
      using client_type = client::SinesClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufSines()  {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufSines, &BufSines::process, &BufSines::postProcess, &BufSines::postComplete>(world, "/BufSines", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        client::SinesClient::ProcessModel processModel;
        std::string whatHappened;//this will give us a message to pass back if param check fails
        std::tie(parametersOk,whatHappened,processModel) = processor.sanityCheck();
        if(!parametersOk)
        {
          Print("fdNMF: %s \n", whatHappened.c_str());
          return false;
        }
        //Now, we can proceed
        processor.process(processModel);
        mModel = processModel;
        return true;
      }

      bool postProcess(World* world)
      {
        static_cast<SCBufferView*>(mModel.sine)->assignToRT(world);
        static_cast<SCBufferView*>(mModel.res)->assignToRT(world);
        return true;
      }
      
      bool postComplete(World* w) {
        static_cast<SCBufferView*>(mModel.sine)->cleanUp();
        static_cast<SCBufferView*>(mModel.res)->cleanUp();
        return true;
      }
      
      std::vector<client::Instance>& parameters()
      {
        return processor.getParams();
      }
    private:
      client::SinesClient processor;
      client::SinesClient::ProcessModel mModel;
    };//class
  } //namespace wrapper
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::wrapper::BufSines,fluid::client::SinesClient>(ft, "BufSines");
  fluid::wrapper::printCmd<fluid::client::SinesClient>(ft,"BufSines","FDSines");
}
