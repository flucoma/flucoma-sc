  // FD_BufHPSS, an NRT buffer HPSS Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "fdNRTBase.hpp"
#include "algorithms/STFT.hpp"
#include "data/FluidTensor.hpp"
#include "clients/nrt/HPSS.hpp"
#include "clients/common/FluidParams.hpp"
#include "SC_PlugIn.h"
#include <unordered_set>
#include <vector>

static InterfaceTable *ft;

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;

namespace fluid {
  namespace sc{
    
    class BufHPSS: public NRTCommandBase
    {
      /*
       - srcbuf num
       – src start frame
       - src numframes
       – src start chan
       – src num chans
       – harms dst
       – perc dst
       – window size
       – hop size
       – fft size
       */
    public:
      using client_type = hpss::HPSSClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufHPSS()  {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufHPSS, &BufHPSS::process, &BufHPSS::postProcess, &BufHPSS::postComplete>(world, "AsyncNMF", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        hpss::HPSSClient::ProcessModel processModel;
        std::string whatHappened;//this will give us a message to pass back if param check fails
        std::tie(parametersOk,whatHappened,processModel) = processor.sanityCheck();
        if(!parametersOk)
        {
          Print("fdHPSS: %s \n", whatHappened.c_str());
          return false;
        }
        //Now, we can proceed
        processor.process(processModel);
        mModel = processModel;
        return true;
      }

      bool postProcess(World* world)
      {
        static_cast<SCBufferView*>
            (parameter::lookupParam("harmbuf", processor.getParams()).getBuffer())->assignToRT(world);
        static_cast<SCBufferView*>
            (parameter::lookupParam("percbuf", processor.getParams()).getBuffer())->assignToRT(world);
        return true;
      }
      
      bool postComplete(World* w) { return true; }
      std::vector<parameter::Instance>& parameters()
      {
        return processor.getParams();
      }
    private:
      hpss::HPSSClient processor;
      hpss::HPSSClient::ProcessModel mModel;
    };//class
  } //namespace sc
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::sc::BufHPSS,fluid::hpss::HPSSClient>(ft, "BufHPSS");
  fluid::sc::printCmd<fluid::hpss::HPSSClient>(ft,"BufHPSS","FDHPSS");
}
