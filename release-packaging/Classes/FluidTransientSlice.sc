FluidTransientSlice : UGen {
	*ar { arg in = 0, order = 20, blockSize = 256, padSize = 128, skew = 0.0, threshFwd = 3.0, threshBack = 1.1, winSize=14, debounce=25, minSlice = 1000;
		^this.multiNew('audio', in.asAudioRateInput(this), order, blockSize, padSize, skew, threshFwd ,threshBack, winSize, debounce, minSlice)
	}
}