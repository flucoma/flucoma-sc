FluidOnsetSlice : UGen {
	*ar { arg in = 0, function = 0, threshold = 0.1, debounce = 2, filterSize = 5, frameDelta = 0, winSize = 1024, hopSize = 256, fftSize = 1024, maFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), function, threshold, debounce, filterSize, frameDelta, winSize, hopSize, fftSize, maFFTSize)
	}
}
