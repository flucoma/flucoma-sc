FluidAudioTransport : UGen {
	*ar { arg in = 0, interpolation=0.0, bandwidth=255,windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), interpolation, bandwidth, windowSize, hopSize, fftSize, maxFFTSize)
	}
}
