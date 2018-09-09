FDGain : UGen {
	*ar { arg in = 0, frameSize=64, gain=1.0;
		^this.multiNew('audio', in.asAudioRateInput(this),frameSize, gain)
	}
}

