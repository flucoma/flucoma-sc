#pragma once 

#include "SCBufferAdaptor.hpp"

namespace fluid {
namespace client {
namespace impl {
  
  template <size_t N, typename T>
  struct AssignBuffer
  {
    void operator()(const typename BufferT::type& p, World* w)
    {
      if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->assignToRT(w);
    }
  };
  
  template <size_t N, typename T>
  struct CleanUpBuffer
  {
    void operator()(const typename BufferT::type& p)
    {
      if (auto b = static_cast<SCBufferAdaptor*>(p.get())) b->cleanUp();
    }
  };  
  
}
}
}
