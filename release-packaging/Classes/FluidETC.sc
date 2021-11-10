FluidETC : UGen {
	*ar { arg in = 0;
		^this.multiNew('audio', in.asAudioRateInput(this))
	}
}

