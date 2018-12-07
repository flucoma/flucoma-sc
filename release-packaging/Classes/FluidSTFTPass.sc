FluidSTFTPass : UGen {
	*ar { arg in = 0, windowSize= 1024, hopSize= 256, fftSize= -1, maxWinSize= 16384;
		^this.multiNew('audio', in.asAudioRateInput(this),windowSize, hopSize, fftSize, maxWinSize)
	}
}
