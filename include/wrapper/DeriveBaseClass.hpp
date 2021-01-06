#pragma once 

#include "NonRealtime.hpp"
#include "Realtime.hpp"

namespace fluid {
namespace client {
  
template <typename Client> class FluidSCWrapper;  
  
namespace impl { 

template<bool UseRealTime> struct ChooseRTOrNRT;

template<>
struct ChooseRTOrNRT<false>
{
   template<typename Client, typename Wrapper>
   using type = NonRealTime<Client,Wrapper>;
};

template<>
struct ChooseRTOrNRT<true>
{
   template<typename Client, typename Wrapper>
   using type = RealTime<Client,Wrapper>;
};


template <typename Client,typename Wrapper>   
struct BaseChooser
{
  using RT = typename Client::isRealTime;
  
  static constexpr bool UseRealTime = RT::value && !IsModel_t<Client>::value;
  
  using type = typename ChooseRTOrNRT<UseRealTime>::template type<Client,Wrapper>;
}; 


template <typename Client,typename Wrapper>
using BaseChooser_t = typename BaseChooser<Client,Wrapper>::type;


template <typename Client>
using FluidSCWrapperBase = BaseChooser_t<Client,FluidSCWrapper<Client>>;
} 
}
}
