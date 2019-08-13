
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <FluidSCWrapper.hpp>
#include <clients/rt/GainClient.hpp>

static InterfaceTable *ft;

PluginLoad(FluidGainUgen)
{
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<RTGainClient>("FluidGain", ft);
}
