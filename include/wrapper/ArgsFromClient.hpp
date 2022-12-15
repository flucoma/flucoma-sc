#pragma once 

#include "Meta.hpp"
#include <data/FluidMemory.hpp>
#include <fmt/format.h>

namespace fluid {
namespace client {

namespace impl {  
  // Iterate over kr/ir inputs via callbacks from params object
  struct FloatControlsIter
  {
    FloatControlsIter(float** vals, index N) : mValues(vals), mSize(N) {}

    float next() { return mCount >= mSize ? 0 : *mValues[mCount++]; }

    void reset(float** vals)
    {
      mValues = vals;
      mCount = 0;
    }

    index size() const noexcept { return mSize; }
    index remain() { return mSize - mCount; }
    
  private:
    float** mValues;
    index   mSize;
    index   mCount{0};
  };  
} //impl

//Specializations of param reader for RT and NRT cases (data encoded differently, buffer semantics differ cause of local bufs)
template <typename ArgType> struct ParamReader;

// RT case: we're decoding data from float**, there will be a Unit, we can have LocalBufs
// TODO: All the allocations should be using SC RT allocator, but this won't work reliably until it propagates down through the param set
template<>
struct ParamReader<impl::FloatControlsIter>
{

  using Controls = impl::FloatControlsIter;

  /// todo: fix std::string to use a specialisation with RT alloc
  template <typename Alloc>
  static auto
  fromArgs(Unit* /*x*/, Controls& args,
           std::basic_string<char, std::char_traits<char>, Alloc> const&, int)
  {
    // first is string size, then chars
    index size = static_cast<index>(args.next());
    std::string res;
    res.resize(asUnsigned(size));
    for (index i = 0; i < size; ++i)
      res[asUnsigned(i)] = static_cast<char>(args.next());
    return res;
  }

  static auto fromArgs(Unit*, Controls& args,typename LongArrayT::type&, int)
  {
      //first is array size, then items
      using Container = typename LongArrayT::type;
      using Value = typename Container::type;
      index size = static_cast<index>(args.next());
      /// todo: fix allocator
      Container res(size, FluidDefaultAllocator());
      for (index i = 0; i < size; ++i)
        res[i] = static_cast<Value>(args.next());
      return res;
  }
  
  template <typename T>
  static std::enable_if_t<std::is_integral<T>::value, T>
  fromArgs(Unit*, Controls& args, T, int)
  {
    return static_cast<index>(args.next());
  }

  template <typename T>
  static std::enable_if_t<std::is_floating_point<T>::value, T>
  fromArgs(Unit*, Controls& args, T, int)
  {
    return args.next();
  }
  
  static auto fromArgs(Unit*, Controls& args, typename ChoicesT::type, int)
  {
     return typename ChoicesT::type(std::size_t(static_cast<index>(args.next())));
  }
  
  static SCBufferAdaptor* fetchBuffer(Unit* x, index bufnum)
  {
    if(bufnum >= x->mWorld->mNumSndBufs)
    {
      index localBufNum = bufnum - x->mWorld->mNumSndBufs;
      
      Graph* parent = x->mParent;
      
      return localBufNum <= parent->localMaxBufNum ?
                                new SCBufferAdaptor(parent->mLocalSndBufs + localBufNum,x->mWorld,true)
                                : nullptr;
    }
    else
      return bufnum >= 0 ? new SCBufferAdaptor(bufnum, x->mWorld) : nullptr;
  }

  static auto fromArgs(Unit* x, Controls& args, BufferT::type&, int)
  {
    typename LongT::type bufnum = static_cast<typename LongT::type>(
        ParamReader::fromArgs(x, args, typename LongT::type(), -1));
    return BufferT::type(fetchBuffer(x, bufnum));
  }

  static auto fromArgs(Unit* x, Controls& args, InputBufferT::type&, int)
  {
    typename LongT::type bufnum =
        static_cast<LongT::type>(fromArgs(x, args, LongT::type(), -1));
    return InputBufferT::type(fetchBuffer(x, bufnum));
  }

  template <typename P>
  static std::enable_if_t<IsSharedClient<P>::value, P>
  fromArgs(Unit* x, Controls& args, P&, int)
  {
    auto id = fromArgs(x, args, index{}, 0);
    return  {id >= 0 ? std::to_string(id).c_str() : "" };
  }

  static auto fromArgs(Unit*, Controls& args, typename LongRuntimeMaxT::type&,
                       int)
  {
    return typename LongRuntimeMaxT::type{static_cast<index>(args.next()),
                                          static_cast<index>(args.next())};
  }
};

// NRT case: we're decoding data from sc_msg_iter*, there will be a World*, we can't have LocalBufs
// TODO: All the allocations should be using SC RT allocator (I guess: this will probably always run on the RT thread), but this won't work reliably until it propagates down through the param set
template<>
struct ParamReader<sc_msg_iter>
{
  static const char* oscTagToString(char tag)
  {
    switch (tag)
    {
      case 'i': return "integer"; break;
      case 'f': return "float"; break;
      case 'd': return "double"; break;
      case 's': return "string"; break;
      case 'b': return "blob"; break;
      case 't': return "time tag"; break;
      default: return "unknown type";
    }
  }
  
  
  template <typename T>
  static const char* argTypeToString(Optional<T>&)
  {
    return argTypeToString(T{});
  }
  
  static const char* argTypeToString(const std::string&)
  {
    return "string";
  }
  
  template <typename T>
  static std::enable_if_t<std::is_integral<T>::value, const char*>
  argTypeToString(T)
  {
    return "integer";
  }

  template <typename T>
  static std::enable_if_t<std::is_floating_point<T>::value, const char*>
  argTypeToString(T)
  {
    return "float";
  }

  static const char* argTypeToString(const BufferT::type&)
  {
    return "buffer";
  }
  
  static const char* argTypeToString(const InputBufferT::type&)
  {
    return "buffer";
  }
  
  template <typename P>
  static std::enable_if_t<IsSharedClient<P>::value,const char*>
  argTypeToString(const P&)
  {
    return "shared_object"; //not ideal
  }

  static bool argTypeOK(std::string&, char tag)
  {
    return tag == 's';
  }
  
  template <typename T>
  static std::enable_if_t<std::is_integral<T>::value
                          || std::is_floating_point<T>::value, bool>
  argTypeOK(T, char tag)
  {
    return tag == 'i' || tag == 'f' || tag == 'd';
  }

  static bool argTypeOK(const BufferT::type&, char tag)
  {
    return tag == 'i';
  }
  
  static bool argTypeOK(const InputBufferT::type&, char tag)
  {
    return tag == 'i';
  }
  
  template <typename P>
  static std::enable_if_t<IsSharedClient<P>::value,bool>
  argTypeOK(const P&, char tag)
  {
    return tag == 'i';
  }
  
  template<typename T>
  static bool argTypeOK(const Optional<T>&, char tag)
  {
    return argTypeOK(T{},tag);
  }
  
  template<typename Alloc>
  static auto fromArgs(World*, sc_msg_iter& args, std::basic_string<char,std::char_traits<char>,Alloc> const&, int)
  {
    const char* recv = args.gets("");

    return std::string(recv ? recv : "");
  }
  
  template <typename T>
  static std::enable_if_t<std::is_integral<T>::value, T>
  fromArgs(World*, sc_msg_iter& args, T, int defVal)
  {
    return args.geti(defVal);
  }

  template <typename T>
  static std::enable_if_t<std::is_floating_point<T>::value, T>
  fromArgs(World*, sc_msg_iter& args, T, int)
  {
    return args.getf();
  }
  
  static SCBufferAdaptor* fetchBuffer(World* x, index bufnum)
  {
    if(bufnum >= x->mNumSndBufs)
    {
      std::cout << "ERROR: bufnum " << bufnum << " is invalid for global buffers\n";
      return nullptr;
    }
    else
      return bufnum >= 0 ? new SCBufferAdaptor(bufnum, x) : nullptr;
  }

  static auto fromArgs(World* x, sc_msg_iter& args, BufferT::type&, int)
  {
    typename LongT::type bufnum = static_cast<typename LongT::type>(
        ParamReader::fromArgs(x, args, typename LongT::type(), -1));
    return BufferT::type(fetchBuffer(x, bufnum));
  }

  static auto fromArgs(World* x, sc_msg_iter& args, InputBufferT::type&, int)
  {
    typename LongT::type bufnum =
        static_cast<LongT::type>(fromArgs(x, args, LongT::type(), -1));
    return InputBufferT::type(fetchBuffer(x, bufnum));
  }

  template <typename P>
  static std::enable_if_t<IsSharedClient<P>::value, P>
  fromArgs(World* x, sc_msg_iter& args, P&, int)
  {
    auto id = fromArgs(x, args, index{}, 0); 
    return {id >= 0 ? std::to_string(id).c_str() : ""};
  }
  
  static auto fromArgs(World*, sc_msg_iter& args,typename LongArrayT::type&, int)
  {
      //first is array size, then items
      using Container = typename LongArrayT::type;
      using Value = typename Container::type;
      index size = static_cast<index>(args.geti());
      Container res(size, FluidDefaultAllocator());
      for (index i = 0; i < size; ++i)
        res[i] = static_cast<Value>(args.geti());
      return res;
  }
  
  static auto fromArgs(World*,sc_msg_iter& args,typename LongRuntimeMaxT::type&, int)
  {
      return typename LongRuntimeMaxT::type{args.geti(), args.geti()}; 
  }

  static auto fromArgs(World*, sc_msg_iter& args, typename ChoicesT::type, int)
  {
     int x = args.geti();
     return typename ChoicesT::type(asUnsigned(x));
  }

  template<typename T>
  static auto fromArgs(World* w, sc_msg_iter& args, Optional<T>, int)
  {
    return Optional<T>{fromArgs(w,args,T{},int{})};
  }
};


template <typename Wrapper>
struct ClientParams{
// Iterate over arguments via callbacks from params object
  template <typename ArgType, size_t N, typename T>
  struct Setter
  {
    static constexpr index argSize =
        Wrapper::Client::getParameterDescriptors().template get<N>().fixedSize;

    
    /// Grizzly enable_if hackage coming up. Need to brute force an int from incoming data into a string param for FluidDataSet / FluidLabelSet. 
    /// This will go away one day

    template <typename Context, typename Params,
              typename Client = typename Wrapper::Client, size_t Number = N>
    std::enable_if_t<!impl::IsNamedShared_v<Client> || Number != 0,
                     typename T::type>
    operator()(Context* x, ArgType& args, Params& p, Allocator& alloc)
    {
      // Just return default if there's nothing left to grab
      if (args.remain() == 0)
      {
        std::cout << "WARNING: " << Wrapper::getName()
                  << " received fewer parameters than expected\n";
        return Wrapper::Client::getParameterDescriptors().template makeValue<N>(alloc);
      }

      ParamLiteralConvertor<T, argSize> a;
      using LiteralType =
          typename ParamLiteralConvertor<T, argSize>::LiteralType;

      for (index i = 0; i < argSize; i++)
        a[i] = static_cast<LiteralType>(
            ParamReader<ArgType>::fromArgs(x, args, a[0], 0));

      /// He said "I don't like it, but I have to go along with it"
      /// Make sure that the maximum for LongRuntimeMax params is
      /// properly constrained *as soon as possible*
      if constexpr (std::is_same_v<T, LongRuntimeMaxT>)
      {
        auto  param = a.value();
        index maximum = param.maxRaw();
        maximum = p.template applyConstraintToMax<N>(maximum);
        return LongRuntimeMaxParam(param(), maximum);
      }
      else
        return a.value();
    }

    template <typename Context, typename Params,
              typename Client = typename Wrapper::Client, size_t Number = N>
    std::enable_if_t<impl::IsNamedShared_v<Client> && Number == 0,
                     typename T::type>
    operator()(Context* x, ArgType& args, Params&, Allocator& alloc)
    {
      // Just return default if there's nothing left to grab
      if (args.remain() == 0)
      {
        std::cout << "WARNING: " << Wrapper::getName()
                  << " received fewer parameters than expected\n";
        return Wrapper::Client::getParameterDescriptors().template makeValue<N>(alloc);
      }
      
      index id = ParamReader<ArgType>::fromArgs(x,args,index{},0);
      using StdAlloc = foonathan::memory::std_allocator<char, Allocator>;
      using fmt_memory_buffer =
          fmt::basic_memory_buffer<char, fmt::inline_buffer_size, StdAlloc>;
      auto             buf = fmt_memory_buffer(alloc);
      std::string_view fmt_string("{}");
      fmt::vformat_to(std::back_inserter(buf), fmt_string,
                      fmt::make_format_args(id));
      return rt::string(buf.data(), buf.size(), alloc);
    }
  };
  
  template <typename ArgType, size_t N, typename T>
  struct Getter
  {
    static constexpr index argSize =
        Wrapper::Client::getParameterDescriptors().template get<N>().fixedSize;

  };
  
  
}; 

}
}
