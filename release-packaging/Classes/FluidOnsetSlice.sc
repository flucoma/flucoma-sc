FluidOnsetSlice : UGen {
	*ar { arg in = 0, function = 0, thresh = 0.5, debounce = 2, filtSize = 5, frameDelta = 0, winSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), function, thresh, debounce, filtSize, winSize, hopSize, frameDelta, fftSize, maxFFTSize)
	}
}
