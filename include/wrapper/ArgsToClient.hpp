#pragma once 

namespace fluid {
namespace client {

  struct ToFloatArray
  {
    static index allocSize(typename BufferT::type) { return 1; }

    template <typename T>
    static std::enable_if_t<
        std::is_integral<T>::value || std::is_floating_point<T>::value, index>
        allocSize(T)
    {
      return 1;
    }

    static index allocSize(std::string s)
    {
      return asSigned(s.size()) + 1;
    } // put null char at end when we send

    static index allocSize(FluidTensor<std::string, 1> s)
    {
      index count = 0;
      for (auto& str : s) count += (str.size() + 1);
      return count;
    }

    template <typename T>
    static index allocSize(FluidTensor<T, 1> s)
    {
      return s.size();
    }

    template <typename... Ts>
    static std::tuple<std::array<index, sizeof...(Ts)>, index>
    allocSize(std::tuple<Ts...>&& t)
    {
      return allocSizeImpl(std::forward<decltype(t)>(t),
                           std::index_sequence_for<Ts...>());
    };

    template <typename... Ts, size_t... Is>
    static std::tuple<std::array<index, sizeof...(Ts)>, index>
    allocSizeImpl(std::tuple<Ts...>&& t, std::index_sequence<Is...>)
    {
      index                            size{0};
      std::array<index, sizeof...(Ts)> res;
      (void) std::initializer_list<int>{
          (res[Is] = size, size += ToFloatArray::allocSize(std::get<Is>(t)),
           0)...};
      return std::make_tuple(res,
                             size); // array of offsets into allocated buffer &
                                    // total number of floats to alloc
    };

    static void convert(float* f, typename BufferT::type buf)
    {
      f[0] = static_cast<SCBufferAdaptor*>(buf.get())->bufnum();
    }

    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value ||
                            std::is_floating_point<T>::value>
    convert(float* f, T x)
    {
      f[0] = static_cast<float>(x);
    }

    static void convert(float* f, std::string s)
    {
      std::copy(s.begin(), s.end(), f);
      f[s.size()] = 0; // terminate
    }
    static void convert(float* f, FluidTensor<std::string, 1> s)
    {
      for (auto& str : s)
      {
        std::copy(str.begin(), str.end(), f);
        f += str.size();
        *f++ = 0;
      }
    }
    template <typename T>
    static void convert(float* f, FluidTensor<T, 1> s)
    {
      static_assert(std::is_convertible<T, float>::value,
                    "Can't convert this to float output");
      std::copy(s.begin(), s.end(), f);
    }

    template <typename... Ts, size_t... Is>
    static void convert(float* f, std::tuple<Ts...>&& t,
                        std::array<index, sizeof...(Ts)> offsets,
                        std::index_sequence<Is...>)
    {
      (void) std::initializer_list<int>{
          (convert(f + offsets[Is], std::get<Is>(t)), 0)...};
    }
  };
  
  template<typename Packet>
  struct ToOSCTypes
  {
  
    static index numTags(typename BufferT::type) { return 1; }

    template <typename T>
    static std::enable_if_t<
        std::is_integral<T>::value || std::is_floating_point<T>::value, index>
        numTags(T)
    {
      return 1;
    }

    static index numTags(std::string)
    {
      return 1;;
    }

    template <typename T>
    static index numTags(FluidTensor<T, 1> s)
    {
      return s.size();
    }

    template <typename... Ts>
    static index numTags(std::tuple<Ts...>&&)
    {
      return std::tuple_size<std::tuple<Ts...>>::value;
    };
  
  
    static void getTag(Packet& p, typename BufferT::type) { p.addtag('i'); }
        
    template <typename T>
    static std::enable_if_t<std::is_integral<std::decay_t<T>>::value>
    getTag(Packet& p, T&&) { p.addtag('i'); }

    template <typename T>
    static std::enable_if_t<std::is_floating_point<std::decay_t<T>>::value>
    getTag(Packet& p, T&&) { p.addtag('f'); }

    static void getTag (Packet& p, std::string) { p.addtag('s'); }

    template <typename T>
    static void getTag(Packet& p, FluidTensor<T, 1> x)
    {
      T dummy{};
      for (int i = 0; i < x.rows(); i++)
        getTag(p, dummy);
   }

    template <typename... Ts>
    static void getTag(Packet& p, std::tuple<Ts...>&& t)
    {
        ForEach(t,[&p](auto&  x){getTag(p,x);});
    }


    static void convert(Packet& p, typename BufferT::type buf)
    {
      p.addi(static_cast<SCBufferAdaptor*>(buf.get())->bufnum());
    }

    template <typename T>
    static std::enable_if_t<std::is_integral<T>::value>
    convert(Packet& p, T x)
    {
      p.addi(x);
    }

    template <typename T>
    static std::enable_if_t<std::is_floating_point<T>::value>
    convert(Packet& p, T x)
    {
      p.addf(x);
    }

    static void convert(Packet& p, std::string s)
    {
      p.adds(s.c_str());
    }
    
    template <typename T>
    static void convert(Packet& p, FluidTensor<T, 1> s)
    {
      for(auto& x: s) convert(p,x);
    }

    template <typename... Ts>
    static void convert(Packet& p, std::tuple<Ts...>&& t)
    {
       ForEach(t,[&p](auto& x){ convert(p,x);});
    }
  };
  
  
}
}
