FluidGain : UGen {
	*ar { arg in = 0, gain=1.0;
		^this.multiNew('audio', in.asAudioRateInput(this), gain)
	}
}

