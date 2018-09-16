  // FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "fdNRTBase.hpp"

#include "algorithms/STFT.hpp"
#include "data/FluidTensor.hpp"
#include "clients/nrt/NMFClient.hpp"
#include "clients/common/FluidParams.hpp"


#include "SC_PlugIn.h"

#include <unordered_set>
#include <vector>

static InterfaceTable *ft;

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;
using fluid::nmf::NMFClient;

namespace fluid {
  namespace sc{
    
    class BufNMF: public NRTCommandBase
    {
      /*
       - srcbuf num
       – src start frame
       - src numframes
       – src start chan
       – src num chans
       – resynths dst
       – dicts dst
       – acts dst
       - 'overwrite' flag [-1:1]
       
       - rank
       - iterations
       – window size
       – hop size
       – fft size
       -– boundary flag
       -– rand seed
       */
  
    public:
      using client_type = NMFClient;
      using NRTCommandBase::NRTCommandBase;

      
      ~BufNMF()
      {
//        if(src)     delete src;
//        if(resynth) delete resynth;
//        if(dict)    delete dict;
//        if(act)     delete act;
      }
      
      void runCommand(World* world, void* replyAddr, char* completionMsgData, size_t completionMsgSize)
      {
        cmd<BufNMF, &BufNMF::process, &BufNMF::postProcess, &BufNMF::postComplete>(world, "/BufNMF", replyAddr,  completionMsgData, completionMsgSize);
      }
      
      bool process(World* world)
      {
        //sanity check the parameters
        bool parametersOk;
        NMFClient::ProcessModel processModel;
        std::string whatHappened;//this will give us a message to pass back if param check fails
        std::tie(parametersOk,whatHappened,processModel) = nmf.sanityCheck();
        if(!parametersOk)
        {
          Print("fdNMF: %s \n", whatHappened.c_str());
          return false;
        }
        //Now, we can proceed
        
        nmf.process(processModel);
        mModel = processModel;

        src     = static_cast<SCBufferView*>(parameter::lookupParam("src",      nmf.getParams()).getBuffer());
        resynth = static_cast<SCBufferView*>(parameter::lookupParam("resynthbuf", nmf.getParams()).getBuffer());
        dict    = static_cast<SCBufferView*>(parameter::lookupParam("filterbuf",  nmf.getParams()).getBuffer());
        act     = static_cast<SCBufferView*>(parameter::lookupParam("envbuf",  nmf.getParams()).getBuffer());

        return true;
      }

      bool postProcess(World* world)
      {
        
    
        if(mModel.resynthesise)
          resynth->assignToRT(world);
        if(mModel.returnDictionaries)
          dict->assignToRT(world);
        if(mModel.returnActivations)
          act->assignToRT(world);
        
        return true;
      }
      
      bool postComplete(World*) {
        if(mModel.resynthesise)
          resynth->cleanUp();
        if(mModel.returnDictionaries)
          dict->cleanUp();
        if(mModel.returnActivations)
          act->cleanUp();
        return true;
      }
      
      std::vector<parameter::Instance>& parameters()
      {
        return nmf.getParams(); 
      }
    private:
      NMFClient nmf;
      NMFClient::ProcessModel mModel;
      SCBufferView* src;
      SCBufferView* resynth;
      SCBufferView* dict;
      SCBufferView* act;
    };//class
  } //namespace sc
}//namespace fluid


PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  registerCommand<fluid::sc::BufNMF,fluid::nmf::NMFClient>(ft, "BufNMF");
}
