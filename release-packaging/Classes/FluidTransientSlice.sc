FluidTransientSlice : FluidRTUGen {
	*ar { arg in = 0, order = 20, blockSize = 256, padSize = 128, skew = 0.0, threshFwd = 2.0, threshBack = 1.1, windowSize=14, clumpLength=25, minSliceLength = 1000;
		^this.multiNew('audio', in.asAudioRateInput(this), order, blockSize, padSize, skew, threshFwd ,threshBack, windowSize, clumpLength, minSliceLength)
	}
}
