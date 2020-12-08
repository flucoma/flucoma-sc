#pragma once 

#include "NonRealtime.hpp"
#include "Realtime.hpp"

namespace fluid {
namespace client {
  
template <typename Client> class FluidSCWrapper;  
  
namespace impl { 
  
template <typename Client,typename Wrapper>   
struct BaseChooser
{
  template<bool>struct Choose
  {
    using type = NonRealTime<Client,Wrapper>;
  };
  
  template<>
  struct Choose<true>
  {
    using type = RealTime<Client,Wrapper>;
  };
  
  using RT = typename Client::isRealTime;
  
  static constexpr bool UseRealTime = RT::value && !IsModel_t<Client>::value;
  
  using type = typename Choose<UseRealTime>::type;
}; 

template <typename Client,typename Wrapper>
using BaseChooser_t = typename BaseChooser<Client,Wrapper>::type;


template <typename Client>
using FluidSCWrapperBase = BaseChooser_t<Client,FluidSCWrapper<Client>>;
} 
}
}
