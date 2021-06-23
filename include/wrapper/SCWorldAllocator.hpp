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

template <typename T>
class SCWorldAllocator
{
  World*          mWorld{nullptr};
  InterfaceTable* mInterface{nullptr};

public:
  using propagate_on_container_move_assignment = std::true_type;
  using value_type = T;

  template <typename U>
  friend class SCWorldAllocator;

  SCWorldAllocator(World* w, InterfaceTable* ft) : mWorld{w}, mInterface{ft} {}

  template <typename U>
  SCWorldAllocator(const SCWorldAllocator<U>& other) noexcept
      : mWorld{other.mWorld}, mInterface{other.mInterface}
  {}

  T* allocate(std::size_t n)
  {
    if (n > std::numeric_limits<std::size_t>::max() / sizeof(T))
      throw std::bad_array_new_length();

    if (mWorld && mInterface)
      if (auto p = static_cast<T*>(mInterface->fRTAlloc(mWorld, n * sizeof(T))))
        return p;

    throw std::bad_alloc();
  }

  void deallocate(T* p, std::size_t n) noexcept
  {
    assert(mWorld && mInterface);
    mInterface->fRTFree(mWorld, p);
  }
};
} // namespace fluid
