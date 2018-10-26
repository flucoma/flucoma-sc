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
  namespace wrapper{
    
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
      using client_type = client::HPSSClient;
      using NRTCommandBase::NRTCommandBase;
      
      ~BufHPSS()  {}
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufHPSS, &BufHPSS::process, &BufHPSS::postProcess, &BufHPSS::postComplete>(world, "/BufHPSS", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        client::HPSSClient::ProcessModel processModel;
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
        static_cast<SCBufferView*>(mModel.harm)->assignToRT(world);
        static_cast<SCBufferView*>(mModel.perc)->assignToRT(world);
        
        if(mModel.res)
        static_cast<SCBufferView*>(mModel.res)->assignToRT(world);
        
        return true;
      }
      
      bool postComplete(World*) {
        static_cast<SCBufferView*>(mModel.harm)->cleanUp();
        static_cast<SCBufferView*>(mModel.perc)->cleanUp();
        if(mModel.res)
          static_cast<SCBufferView*>(mModel.res)->cleanUp();
        return true;
      }
      
      
      std::vector<client::Instance>& parameters()
      {
        return processor.getParams();
      }
    private:
      client::HPSSClient processor;
      client::HPSSClient::ProcessModel mModel;
    };//class
  } //namespace wrapper
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::wrapper::BufHPSS,fluid::client::HPSSClient>(ft, "BufHPSS");
  fluid::wrapper::printCmd<fluid::client::HPSSClient>(ft,"BufHPSS","FDHPSS");
}
