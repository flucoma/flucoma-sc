  // FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)


#include "clients/nrt/NoveltyClient.hpp"
#include "fdNRTBase.hpp"
#include "data/FluidTensor.hpp"
#include "clients/common/FluidParams.hpp"
//#include "SC_PlugIn.h"
//#include <unordered_set>
//#include <vector>

static InterfaceTable *ft;

namespace fluid {
  namespace sc{
    
    class BufNoveltySlice: public NRTCommandBase
    {
    public:
      using client_type = segmentation::NoveltyClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufNoveltySlice() {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufNoveltySlice, &BufNoveltySlice::process, &BufNoveltySlice::postProcess, &BufNoveltySlice::postComplete>(world, "/BufNoveltySlice", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        client_type::ProcessModel processModel;
        std::string whatHappened;//this will give us a message to pass back if param check fails
        std::tie(parametersOk,whatHappened,processModel) = trans.sanityCheck();
        if(!parametersOk)
        {
          Print("FluidBufNovletySlice: %s \n", whatHappened.c_str());
          return false;
        }
        trans.process(processModel);
        mModel = processModel;
        return true;
      }

      bool postProcess(World* world)
      {
        static_cast<SCBufferView*>(mModel.indices)->assignToRT(world);
        return true;
      }
      
      bool postComplete(World*) {
        static_cast<SCBufferView*>(mModel.indices)->cleanUp();
        return true;
      }
      
      std::vector<parameter::Instance>& parameters()
      {
        return trans.getParams(); 
      }
    private:
      client_type trans;
      client_type::ProcessModel mModel;
    };//class
  } //namespace sc
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::sc::BufNoveltySlice,fluid::segmentation::NoveltyClient>(ft, "BufNoveltySlice");
  fluid::sc::printCmd<fluid::segmentation::NoveltyClient>(ft,"BufNoveltySlice","FluidBufNoveltySlice");
}
