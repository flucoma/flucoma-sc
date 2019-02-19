FluidOnsetSlice : UGen {
	*ar { arg in = 0, function = 0, threshold = 0.1, debounce = 2, filterSize = 5, winSize = 1024, hopSize = 256, frameDelta = 0, fftSize = 1024, maFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), function, threshold, debounce, filterSize, winSize, hopSize, frameDelta, fftSize, maFFTSize)
	}
}
