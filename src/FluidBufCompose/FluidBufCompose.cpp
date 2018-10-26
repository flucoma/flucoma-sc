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
  namespace wrapper{
    
    class BufCompose: public NRTCommandBase
    {
    public:
      using client_type =  client::BufferComposeClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufCompose() {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufCompose, &BufCompose::process, &BufCompose::postProcess, &BufCompose::postComplete>(world, "/BufCompose", replyAddr,  completionMsgData, completionMsgSize);
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
      
      bool postComplete(World* w) {
        static_cast<SCBufferView*>(mModel.dst)->cleanUp();        
        return true;
      }
      
      std::vector<client::Instance>& parameters()
      {
        return bufferCompose.getParams(); 
      }
    private:
      client_type bufferCompose;
      client_type::ProcessModel mModel;
    };//class
  } //namespace wrapper
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::wrapper::BufCompose,fluid:: client::BufferComposeClient>(ft, "BufCompose");
  fluid::wrapper::printCmd<fluid::client::BufferComposeClient>(ft,"BufCompose","FDCompose");
}
