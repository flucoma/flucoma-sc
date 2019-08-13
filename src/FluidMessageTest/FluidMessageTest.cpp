
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <clients/nrt/MessageTest.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(FluidSTFTUGen) {
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTThreadingMessageTest>("FluidMessageTest", ft);
}
