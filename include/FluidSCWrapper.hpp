/*
Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)
Copyright University of Huddersfield.
Licensed under the BSD-3 License.
See license.md file in the project root for full license information.
This project has received funding from the European Research Council (ERC)
under the European Union’s Horizon 2020 research and innovation programme
(grant agreement No 725899).
*/

#pragma once

#include "SCBufferAdaptor.hpp"
#include "wrapper/DeriveBaseClass.hpp"
#include "wrapper/NonRealtime.hpp"
#include "wrapper/Realtime.hpp"
#include <FluidVersion.hpp>

namespace fluid {
namespace client {

/// The main wrapper
template <typename C>
class FluidSCWrapper : public impl::FluidSCWrapperBase<C>
{
  using FloatControlsIter = impl::FloatControlsIter;
  
  //I would like to template these to something more scaleable, but baby steps
  friend class impl::RealTime<C,FluidSCWrapper>;
  friend class impl::NonRealTime<C,FluidSCWrapper>;

  static void doVersion(Unit*, sc_msg_iter*)
  {
    std::cout << "Fluid Corpus Manipulation Toolkit: version " << fluidVersion()
              << std::endl;
  }

  bool mInit{false};

public:

  template <size_t N, typename T>
  using ArgumentSetter = typename ClientParams<FluidSCWrapper>::template Setter<sc_msg_iter, N, T>;

  template <size_t N, typename T>
  using ControlSetter = typename ClientParams<FluidSCWrapper>::template Setter<FloatControlsIter, N, T>;

  using Client = C;
  using ParamSetType = typename C::ParamSetType;

  static const char* getName(const char* setName = nullptr)
  {
    static const char* name = nullptr;
    return (name = setName ? setName : name);
  }

  static InterfaceTable* getInterfaceTable(InterfaceTable* setTable = nullptr)
  {
    static InterfaceTable* ft = nullptr;
    return (ft = setTable ? setTable : ft);
  }

  static void setup(InterfaceTable* ft, const char* name)
  {
    getName(name);
    getInterfaceTable(ft);
    impl::FluidSCWrapperBase<Client>::setup(ft, name);
    ft->fDefineUnitCmd(name, "version", doVersion);
    
    std::string commandName("/");
    commandName += getName();
    commandName += "/version";
    ft->fDefinePlugInCmd(commandName.c_str(),
      [](World*, void*, sc_msg_iter*, void*){ doVersion(nullptr,nullptr); },
    nullptr);
      std::cout << "done settup " << name << "\n";
  }

  static auto& setParams(Unit* x, ParamSetType& p, FloatControlsIter& inputs,
                         Allocator& alloc, bool constrain = false,
                         bool initialized = true)
  {
    bool verbose = x->mWorld->mVerbosity > 0;
    
    using Reportage = decltype(static_cast<FluidSCWrapper*>(x)->mReportage);
    
    Reportage* reportage = initialized ? &(static_cast<FluidSCWrapper*>(x)->mReportage) : new Reportage();

    p.template setParameterValuesRT<ControlSetter>(
        verbose ? reportage : nullptr, x, inputs, p, alloc);
    if (constrain) p.constrainParameterValuesRT(verbose ? reportage : nullptr);
    if(verbose)
    {
      for(auto& r:*reportage)
      {
        if(!r.ok()) printResult(x->mParent->mNode.mWorld, r);
      }
    }
    if(!initialized) delete reportage;
    return p;
  }
  
  static void printResult(World* w,Result& r)
  {
  
    switch (r.status())
    {
      case Result::Status::kWarning: 
      {
        if (!w || w->mVerbosity > 0)
          std::cout << "WARNING: " << getName() << " - " << r.message().c_str() << '\n';
        break;
      }
      case Result::Status::kError: 
      {
        std::cout << "ERROR: " << getName() << " - " << r.message().c_str() << '\n';
        break;
      }
      case Result::Status::kCancelled: 
      {
        std::cout << getName() << ": Task cancelled\n" << '\n';
        break;
      }
      default: 
      {
      }
    }  
  }
  
private:
  std::array<Result, Client::getParameterDescriptors().size()> mReportage;
};

template <typename Client>
void makeSCWrapper(const char* name, InterfaceTable* ft)
{
  FluidSCWrapper<Client>::setup(ft, name);
}

} // namespace client
} // namespace fluid
