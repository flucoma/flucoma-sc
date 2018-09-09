  // FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "clients/nrt/BufferComposeNRT.hpp"
#include "fdNRTBase.hpp"
#include "data/FluidTensor.hpp"
#include "clients/common/FluidParams.hpp"
#include "SC_PlugIn.h"
#include <unordered_set>
#include <vector>

static InterfaceTable *ft;

namespace fluid {
  namespace sc{
    
    class BufTransients: public NRTCommandBase
    {
    public:
      using client_type =  buf::BufferComposeClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufTransients() {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufTransients, &BufTransients::process, &BufTransients::postProcess, &BufTransients::postComplete>(world, "AsyncBufferCompose", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        client_type::ProcessModel processModel;
        std::string whatHappened;//this will give us a message to pass back if param check fails
        std::tie(parametersOk,whatHappened,processModel) = bufferCompose.sanityCheck();
        if(!parametersOk)
        {
          Print("fdCompose: %s \n", whatHappened.c_str());
          return false;
        }
        bufferCompose.process(processModel);
        mModel = processModel;
        return true;
      }

      bool postProcess(World* world)
      {
        static_cast<SCBufferView*>(mModel.dst)->assignToRT(world);
        return true;
      }
      
      bool postComplete(World* w) { return true; }
      std::vector<parameter::Instance>& parameters()
      {
        return bufferCompose.getParams(); 
      }
    private:
      client_type bufferCompose;
      client_type::ProcessModel mModel;
    };//class
  } //namespace sc
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::sc::BufTransients,fluid:: buf::BufferComposeClient>(ft, "BufCompose");
  fluid::sc::printCmd<fluid::buf::BufferComposeClient>(ft,"BufCompose","FDCompose"); 
}
