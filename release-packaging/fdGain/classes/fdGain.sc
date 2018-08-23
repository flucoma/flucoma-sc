fdGain: UGen {
	*ar { arg in = 0, framesize=64, gain=1.0;
		^this.multiNew('audio', in.asAudioRateInput(this),framesize, gain)
	}
}

