/*
 Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)
 Copyright 2017-2019 University of Huddersfield.
 Licensed under the BSD-3 License.
 See license.md file in the project root for full license information.
 This project has received funding from the European Research Council (ERC)
 under the European Union’s Horizon 2020 research and innovation programme
 (grant agreement No 725899).
 */
#pragma once

#include <SC_PlugIn.hpp>
#include <cstdlib>
#include <limits>
#include <new>

namespace fluid {

template <typename T, typename Wrapper>
class SCWorldAllocator
{
public:
  using propagate_on_container_move_assignment = std::true_type;
  using value_type = T;

  template <typename U, typename W>
  friend class SCWorldAllocator;

  SCWorldAllocator() = default; 

  template <typename U,typename W>
  SCWorldAllocator(const SCWorldAllocator<U,W>&) noexcept
  {}

  T* allocate(std::size_t n)
  {
    if (n > std::numeric_limits<std::size_t>::max() / sizeof(T))
      throw std::bad_array_new_length();

    World* world = Wrapper::getWorld();
    InterfaceTable* interface = Wrapper::getInterfaceTable();
  
    if (world && interface)
      if (auto p = static_cast<T*>(interface->fRTAlloc(world, n * sizeof(T))))
        return p;

    throw std::bad_alloc();
  }

  void deallocate(T* p, std::size_t /*n*/) noexcept
  {
    World* world = Wrapper::getWorld();
    InterfaceTable* interface = Wrapper::getInterfaceTable();
    if(world && interface) interface->fRTFree(world, p);
  }
};
} // namespace fluid
