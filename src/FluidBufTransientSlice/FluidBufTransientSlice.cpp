  // FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#define EIGEN_USE_BLAS

#include "clients/nrt/TransientSliceNRT.hpp"
#include "fdNRTBase.hpp"
#include "data/FluidTensor.hpp"
#include "clients/common/FluidParams.hpp"
#include "SC_PlugIn.h"
#include <unordered_set>
#include <vector>

static InterfaceTable *ft;

namespace fluid {
  namespace sc{
    
    class BufTransientsSlice: public NRTCommandBase
    {
    public:
      using client_type = segmentation::TransientSliceNRT;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufTransientsSlice() {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufTransientsSlice, &BufTransientsSlice::process, &BufTransientsSlice::postProcess, &BufTransientsSlice::postComplete>(world, "/BufTransientSlice", replyAddr,  completionMsgData, completionMsgSize);
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
          Print("FluidBufTransientSlice: %s \n", whatHappened.c_str());
          return false;
        }
        trans.process(processModel);
        mModel = processModel;
        return true;
      }

      bool postProcess(World* world)
      {
        static_cast<SCBufferView*>(mModel.trans)->assignToRT(world);
        return true;
      }
      
      bool postComplete(World*)
      {
        static_cast<SCBufferView*>(mModel.trans)->cleanUp();
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
  registerCommand<fluid::sc::BufTransientsSlice,fluid::segmentation::TransientSliceNRT>(ft, "BufTransientSlice");
  fluid::sc::printCmd<fluid::segmentation::TransientSliceNRT>(ft,"BufTransientsSlice","FluidBufTransientSlice");
}
