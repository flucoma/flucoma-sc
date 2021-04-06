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

class DataSetWriterClient : public FluidBaseClient, OfflineIn, OfflineOut {
  enum { kDataSet, kIDPrefix, kIDNumber, kBuffer };
  static constexpr std::initializer_list<index> idNumberDefaults{0, 0};

public:
  FLUID_DECLARE_PARAMS(DataSetClientRef::makeParam("dataSet", "DataSet ID"),
                       StringParam("idPrefix", "ID Prefix"),
                       LongArrayParam("idNumber", "ID Counter Offset",
                                      idNumberDefaults),
                       BufferParam("buf", "Data Buffer"));

  DataSetWriterClient(ParamSetViewType &p) : mParams(p) {}

  template <typename T> Result process(FluidContext &) {
    auto dataset = get<kDataSet>().get();
    if (auto datasetPtr = dataset.lock()) {
      std::string &idPrefix = get<kIDPrefix>();
      auto &idNumberArr = get<kIDNumber>();
      if (idNumberArr.size() != 2)
        return {Result::Status::kError, "ID number malformed"};
      if (idPrefix.size() == 0 && idNumberArr[0] == 0)
        return {Result::Status::kError, "No ID supplied"};

      mStream.clear();
      mStream.seekp(0);

      mStream << idPrefix;

      if (idNumberArr[0] > 0)
        mStream << idNumberArr[1];

      auto buf = get<kBuffer>();
      return datasetPtr->setPoint(mStream.str(), buf);
    } else
      return {Result::Status::kError, "No DataSet"};
  }

private:
  std::ostringstream mStream;
};

using NRTThreadedDataSetWriter =
    NRTThreadingAdaptor<ClientWrapper<DataSetWriterClient>>;
} // namespace client
} // namespace fluid
