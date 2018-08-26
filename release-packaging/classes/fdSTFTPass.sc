FDSTFTPass : UGen {
	*ar { arg in = 0, windowsize=1024, hopsize=256, fftsize=windowsize;
		^this.multiNew('audio', in.asAudioRateInput(this),windowsize, hopsize, fftsize)
	}
}
