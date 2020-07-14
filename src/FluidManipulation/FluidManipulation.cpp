
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <clients/nrt/DataSetClient.hpp>
#include <clients/nrt/DataSetQueryClient.hpp>
#include <clients/nrt/LabelSetClient.hpp>
#include <clients/nrt/KDTreeClient.hpp>
#include <clients/nrt/KMeansClient.hpp>
#include <clients/nrt/KNNClassifierClient.hpp>
#include <clients/nrt/KNNRegressorClient.hpp>
#include <clients/nrt/NormalizeClient.hpp>
#include <clients/nrt/StandardizeClient.hpp>
#include <clients/nrt/PCAClient.hpp>
#include <clients/nrt/MDSClient.hpp>
#include <clients/nrt/MLPRegressorClient.hpp>
#include <clients/rt/AudioTransportClient.hpp>
#include <clients/rt/FluidDataSetWr.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(FluidSTFTUGen)
{
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTThreadedDataSetClient>("FluidDataSet",ft);
  makeSCWrapper<NRTThreadedDataSetQueryClient>("FluidDataSetQuery",ft);
  makeSCWrapper<NRTThreadedLabelSetClient>("FluidLabelSet",ft);
  makeSCWrapper<RTKDTreeClient>("FluidKDTree",ft);
  makeSCWrapper<RTKMeansClient>("FluidKMeans",ft);
  makeSCWrapper<RTKNNClassifierClient>("FluidKNNClassifier",ft);
  makeSCWrapper<RTKNNRegressorClient>("FluidKNNRegressor",ft);
  makeSCWrapper<RTNormalizeClient>("FluidNormalize",ft);
  makeSCWrapper<RTStandardizeClient>("FluidStandardize",ft);
  makeSCWrapper<RTPCAClient>("FluidPCA",ft);
  makeSCWrapper<NRTThreadedMDSClient>("FluidMDS",ft);
  makeSCWrapper<RTAudioTransportClient>("FluidAudioTransport",ft);
  makeSCWrapper<NRTThreadedAudioTransportClient>("FluidBufAudioTransport",ft);
  makeSCWrapper<NRTThreadedDataSetWriter>("FluidDataSetWr", ft);
  makeSCWrapper<NRTThreadedMLPRegressorClient>("FluidMLPRegressor",ft);
}
