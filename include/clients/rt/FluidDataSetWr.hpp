#pragma once

#include <algorithms/KDTree.hpp>
#include <clients/common/FluidBaseClient.hpp>
#include <clients/common/FluidNRTClientWrapper.hpp>
#include <clients/common/MessageSet.hpp>
#include <clients/common/OfflineClient.hpp>
#include <clients/common/ParameterSet.hpp>
#include <clients/common/ParameterTypes.hpp>
#include <clients/common/Result.hpp>
#include <clients/common/SharedClientUtils.hpp>
#include <clients/nrt/CommonResults.hpp>
#include <clients/nrt/DataSetClient.hpp>
#include <data/FluidDataSet.hpp>
#include <data/FluidIndex.hpp>
#include <data/FluidTensor.hpp>
#include <data/TensorTypes.hpp>
#include <string>

namespace fluid {
namespace client {

class DataSetWriterClient : public FluidBaseClient, OfflineIn, OfflineOut
{
public:
  FLUID_DECLARE_PARAMS(StringParam("label", "Label"),
                       BufferParam("buf", "Data Buffer"),
                       DataSetClientRef::makeParam("dataSet", "DataSet Name"));

  DataSetWriterClient(ParamSetViewType& p) : mParams(p) {}

  template <typename T>
  Result process(FluidContext&)
  {
    auto& idx = get<0>();
    auto  buf = get<1>();
    auto  dataset = get<2>().get();
    if (auto datasetPtr = dataset.lock())
      return datasetPtr->addPoint(idx, buf);
    else
      return {Result::Status::kError, "No dataset"};
  }
};

using NRTThreadedDataSetWriter =
    NRTThreadingAdaptor<ClientWrapper<DataSetWriterClient>>;
} // namespace client
} // namespace fluid
