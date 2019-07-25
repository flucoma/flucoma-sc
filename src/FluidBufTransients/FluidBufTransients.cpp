
#include <clients/nrt/FluidNRTClientWrapper.hpp>
#include <clients/rt/TransientClient.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(OfflineFluidDecompositionUGens)
{
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTThreadedTransients>("FluidBufTransients", ft);
}
