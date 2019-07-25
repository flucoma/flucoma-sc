  // FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <clients/rt/TransientSlice.hpp>
#include <clients/nrt/FluidNRTClientWrapper.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable* ft;

PluginLoad(OfflineFluidDecompositionUGens) {
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTThreadedTransientSlice>("FluidBufTransientSlice", ft);
}
