#pragma once

#include <clients/rt/GainClient.hpp>
#include <data/FluidTensor.hpp>

#include <SC_PlugIn.hpp>

#include <tuple>
#include <utility>
#include <vector>
namespace fluid {
namespace client {

namespace impl {
template <typename Client, typename T, size_t N> struct SetterDispatchImpl;
template <typename Client, typename T, size_t N> struct GetterDispatchImpl;
} // namespace impl

template <typename Client, typename... Ts>
class FluidSCWrapper : public SCUnit {
public:
  FluidSCWrapper() {
    mInputConnections.reserve(mClient.audioChannelsIn());
    mOutputConnections.reserve(mClient.audioChannelsOut());
    mAudioInputs.reserve(mClient.audioChannelsIn());
    mAudioOutputs.reserve(mClient.audioChannelsOut());

    for (int i = 0; i < mClient.audioChannelsIn(); ++i) {
      mInputConnections.emplace_back(isAudioRateIn(i));
      mAudioInputs.emplace_back(nullptr, 0, 0);
    }

    const Unit *unit = this;

    for (int i = 0; i < mClient.audioChannelsOut(); ++i) {
      mOutputConnections.emplace_back(BUFLENGTH != 1);
      mAudioOutputs.emplace_back(nullptr, 0, 0);
    }

    mCalcFunc = [](Unit *u, int n) {
      FluidSCWrapper *f = static_cast<FluidSCWrapper *>(u);
      f->next(n);
    };
  }

  void next(int n) {

    setParams(mInBuf + 1); // forward on inputs N + 1 as params

    const Unit *unit = this;
    for (int i = 0; i < mClient.audioChannelsIn(); ++i) {
      if (mInputConnections[i])
        mAudioInputs[i].reset(IN(i), 0, fullBufferSize());
    }

    for (int i = 0; i < mClient.audioChannelsOut(); ++i) {
      if (mOutputConnections[i])
        mAudioOutputs[i].reset(out(i), 0, fullBufferSize());
    }

    mClient.process(mAudioInputs, mAudioOutputs);
  }

  void setParams(float **inputs) {
    setParams(inputs, std::index_sequence_for<Ts...>());
  }

private:
  template <size_t... Is>
  void setParams(float **inputs, std::index_sequence<Is...>) {
    (void)std::initializer_list<int>{
        (impl::SetterDispatchImpl<Client, Ts, Is>::f(mClient, 1, inputs[Is],
                                                     mInputConnections[Is+1]),
         0)...};
  }

  std::vector<bool> mInputConnections;
  std::vector<bool> mOutputConnections;
  std::vector<FluidTensorView<float, 1>> mAudioInputs;
  std::vector<FluidTensorView<float, 1>> mAudioOutputs;

  Client mClient;
};

namespace impl {
template <typename Client, size_t N>
struct SetterDispatchImpl<Client, FloatT, N> {
  static void f(Client &x, long ac, float *av, bool isAudio) {
    if (!isAudio)
      x.template setter<N>()(*av);
  }
};

template <typename Client, size_t N>
struct SetterDispatchImpl<Client, LongT, N> {
  static void f(Client &x, long ac, float *av, bool isAudio) {
    if (!isAudio)
      x.template setter<N>()(*av);
  }
};

template <typename Client, size_t N>
struct SetterDispatchImpl<Client, BufferT, N> {
  static void f(Client *x, long ac, float *av, bool isAudio) {
    if (!isAudio)
      x->template setter<N>()(*av);
  }
};

template <typename Client, size_t N>
struct SetterDispatchImpl<Client, EnumT, N> {
  static void f(Client *x, long ac, float *av, bool isAudio) {
    if (!isAudio)
      x->template setter<N>()(*av);
  }
};

} // namespace impl

template <typename Client, typename... Ts>
void makeSCWrapper(InterfaceTable *ft, const char *className,
                   const std::tuple<Ts...> &params) {
  registerUnit<FluidSCWrapper<Client, typename Ts::first_type...>>(ft,
                                                                   className);
}

} // namespace client
} // namespace fluid

