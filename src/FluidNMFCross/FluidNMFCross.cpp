
#include <clients/nrt/NMFCrossClient.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(OfflineFluidDecompositionUGens)
{
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTNMFCrossClient>("FluidBufNMFCross", ft);
}
