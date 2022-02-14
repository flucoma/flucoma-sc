
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include <clients/nrt/DataSetClient.hpp>
#include <clients/nrt/DataSetQueryClient.hpp>
#include <clients/nrt/LabelSetClient.hpp>
#include <clients/nrt/KDTreeClient.hpp>
#include <clients/nrt/KMeansClient.hpp>
#include <clients/nrt/SKMeansClient.hpp>
#include <clients/nrt/KNNClassifierClient.hpp>
#include <clients/nrt/KNNRegressorClient.hpp>
#include <clients/nrt/NormalizeClient.hpp>
#include <clients/nrt/RobustScaleClient.hpp>
#include <clients/nrt/StandardizeClient.hpp>
#include <clients/nrt/PCAClient.hpp>
#include <clients/nrt/MDSClient.hpp>
#include <clients/nrt/UMAPClient.hpp>
#include <clients/nrt/MLPRegressorClient.hpp>
#include <clients/nrt/MLPClassifierClient.hpp>
#include <clients/rt/FluidDataSetWr.hpp>
#include <clients/nrt/GridClient.hpp>
#include <FluidSCWrapper.hpp>

static InterfaceTable *ft;

PluginLoad(FluidSTFTUGen)
{
  ft = inTable;
  using namespace fluid::client;
  makeSCWrapper<NRTThreadedDataSetClient>("FluidDataSet",ft);
  makeSCWrapper<NRTThreadedDataSetQueryClient>("FluidDataSetQuery",ft);
  makeSCWrapper<NRTThreadedLabelSetClient>("FluidLabelSet",ft);

  makeSCWrapper<NRTThreadedKDTreeClient>("FluidKDTree",ft);
  makeSCWrapper<RTKDTreeQueryClient>("FluidKDTreeQuery",ft);

  makeSCWrapper<NRTThreadedKMeansClient>("FluidKMeans",ft);
  makeSCWrapper<RTKMeansQueryClient>("FluidKMeansQuery",ft);

  makeSCWrapper<NRTThreadedSKMeansClient>("FluidSKMeans",ft);
  makeSCWrapper<RTSKMeansQueryClient>("FluidSKMeansQuery",ft);

  makeSCWrapper<NRTThreadedKNNClassifierClient>("FluidKNNClassifier",ft);
  makeSCWrapper<RTKNNClassifierQueryClient>("FluidKNNClassifierQuery",ft);

  makeSCWrapper<NRTThreadedKNNRegressorClient>("FluidKNNRegressor",ft);
  makeSCWrapper<RTKNNRegressorQueryClient>("FluidKNNRegressorQuery",ft);

  makeSCWrapper<NRTThreadedNormalizeClient>("FluidNormalize",ft);
  makeSCWrapper<RTNormalizeQueryClient>("FluidNormalizeQuery",ft);

  makeSCWrapper<NRTThreadedRobustScaleClient>("FluidRobustScale",ft);
  makeSCWrapper<RTRobustScaleQueryClient>("FluidRobustScaleQuery",ft);

  makeSCWrapper<NRTThreadedStandardizeClient>("FluidStandardize",ft);
  makeSCWrapper<RTStandardizeQueryClient>("FluidStandardizeQuery",ft);

  makeSCWrapper<NRTThreadedPCAClient>("FluidPCA",ft);
  makeSCWrapper<RTPCAQueryClient>("FluidPCAQuery",ft);

  makeSCWrapper<NRTThreadedMDSClient>("FluidMDS",ft);

  makeSCWrapper<NRTThreadedUMAPClient>("FluidUMAP",ft);
  makeSCWrapper<RTUMAPQueryClient>("FluidUMAPQuery",ft);

  makeSCWrapper<NRTThreadedDataSetWriter>("FluidDataSetWr", ft);

  makeSCWrapper<NRTThreadedMLPRegressorClient>("FluidMLPRegressor",ft);
  makeSCWrapper<RTMLPRegressorQueryClient>("FluidMLPRegressorQuery",ft);

  makeSCWrapper<NRTThreadedMLPClassifierClient>("FluidMLPClassifier",ft);
  makeSCWrapper<RTMLPClassifierQueryClient>("FluidMLPClassifierQuery",ft);

  makeSCWrapper<NRTThreadedGridClient>("FluidGrid",ft);
}
