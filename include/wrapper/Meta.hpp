#pragma once 

#include <clients/nrt/FluidSharedInstanceAdaptor.hpp>
#include <clients/common/FluidNRTClientWrapper.hpp>
#include <clients/common/SharedClientUtils.hpp>

namespace fluid {
namespace client {
namespace impl {  
  /// Named, shared clients already have a lookup table in their adaptor class
  template <typename T>
  struct IsNamedShared
  {
    using type = std::false_type;
  };

  //TODO: make less tied to current implementation
  template <typename T>
  struct IsNamedShared<NRTThreadingAdaptor<NRTSharedInstanceAdaptor<T>>>
  {
    using type = std::true_type;
  };

  template<typename T>
  using IsNamedShared_t = typename IsNamedShared<T>::type;

  template<typename T>
  constexpr bool IsNamedShared_v = IsNamedShared_t<T>::value;

  /// Models don't, but still need to survive CMD-.
  template<typename T>
  struct IsModel
  {
    using type = std::false_type;
  };

  template<typename T>
  struct IsModel<NRTThreadingAdaptor<ClientWrapper<T>>>
  {
    using type = typename ClientWrapper<T>::isModelObject;
  };

  template<typename T>
  struct IsModel<ClientWrapper<T>>
  {
    using type = typename ClientWrapper<T>::isModelObject;
  };

  template<typename T>
  using IsModel_t = typename IsModel<T>::type;
  
  template<typename T>
  constexpr bool IsModel_v = IsModel_t<T>::value;
  
  
  
}
}
}
