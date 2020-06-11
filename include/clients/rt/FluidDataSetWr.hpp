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
  FLUID_DECLARE_PARAMS(
      DataSetClientRef::makeParam("dataSet", "DataSet Name"),
      StringParam("labelPrefix","Label Prefix"),
      LongParam("labelOffset", "Label Counter Offset", 0),
      BufferParam("buf", "Data Buffer")
 );

  DataSetWriterClient(ParamSetViewType& p) : mParams(p) {}

  template <typename T>
  Result process(FluidContext&)
  {
    auto  dataset = get<0>().get();
    if (auto datasetPtr = dataset.lock())
    {
      std::stringstream ss;
      ss << get<1>() << get<2>() + (mCounter++);
      auto  buf = get<3>();
      return datasetPtr->addPoint(ss.str(), buf);
    }
    else
      return {Result::Status::kError, "No dataset"};
  }
  
  private:
    index mCounter{0};
};

using NRTThreadedDataSetWriter =
    NRTThreadingAdaptor<ClientWrapper<DataSetWriterClient>>;
} // namespace client
} // namespace fluid
