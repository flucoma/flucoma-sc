#pragma once

#include <flucoma/clients/common/FluidBaseClient.hpp>
#include <flucoma/clients/common/FluidNRTClientWrapper.hpp>
#include <flucoma/clients/common/MessageSet.hpp>
#include <flucoma/clients/common/OfflineClient.hpp>
#include <flucoma/clients/common/ParameterSet.hpp>
#include <flucoma/clients/common/ParameterTypes.hpp>
#include <flucoma/clients/common/Result.hpp>
#include <flucoma/clients/common/SharedClientUtils.hpp>
#include <flucoma/clients/nrt/CommonResults.hpp>
#include <flucoma/clients/nrt/DataSetClient.hpp>
#include <flucoma/data/FluidDataSet.hpp>
#include <flucoma/data/FluidIndex.hpp>
#include <flucoma/data/FluidTensor.hpp>
#include <flucoma/data/TensorTypes.hpp>
#include <string>

namespace fluid {
namespace client {
namespace datasetwr {

enum { kDataSet, kIDPrefix, kIDNumber, kBuffer };

constexpr std::initializer_list<index> idNumberDefaults{0, 0};

constexpr auto DataSetWrParams = defineParameters(
    DataSetClientRef::makeParam("dataSet", "DataSet ID"),
    StringParam("idPrefix", "ID Prefix"),
    LongArrayParam("idNumber", "ID Counter Offset", idNumberDefaults),
    BufferParam("buf", "Data Buffer"));

class DataSetWriterClient : public FluidBaseClient, OfflineIn, OfflineOut {

public:
  using ParamDescType = decltype(DataSetWrParams);

  using ParamSetViewType = ParameterSetView<ParamDescType>;
  std::reference_wrapper<ParamSetViewType> mParams;

  void setParams(ParamSetViewType &p) { mParams = p; }

  template <size_t N> auto &get() const {
    return mParams.get().template get<N>();
  }

  static constexpr auto &getParameterDescriptors() { return DataSetWrParams; }

  DataSetWriterClient(ParamSetViewType &p, FluidContext&) : mParams(p) {}

  template <typename T> Result process(FluidContext &) {
    auto dataset = get<kDataSet>().get();
    if (auto datasetPtr = dataset.lock()) {
      std::string idPrefix = std::string(get<kIDPrefix>());
      auto &idNumberArr = get<kIDNumber>();
      if (idNumberArr.size() != 2)
        return {Result::Status::kError, "ID number malformed"};
      if (idPrefix.size() == 0 && idNumberArr[0] == 0)
        return {Result::Status::kError, "No ID supplied"};

      std::string id = idPrefix;

      if (idNumberArr[0] > 0)
        id += std::to_string(idNumberArr[1]);

      auto buf = get<kBuffer>();
      return datasetPtr->setPoint(id, buf);
    } else
      return {Result::Status::kError, "No DataSet"};
  }
};
} // namespace datasetwr

using NRTThreadedDataSetWriter =
    NRTThreadingAdaptor<ClientWrapper<datasetwr::DataSetWriterClient>>;
} // namespace client
} // namespace fluid
