// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <clients/nrt/BufferComposeNRT.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<BufferComposeClient, float,float>("BufCompose", BufComposeParams, ft); 
//  registerCommand<fluid::wrapper::BufCompose,fluid:: client::BufferComposeClient>(ft, "BufCompose");
//  fluid::wrapper::printCmd<fluid::client::BufferComposeClient>(ft,"BufCompose","FDCompose");
}
