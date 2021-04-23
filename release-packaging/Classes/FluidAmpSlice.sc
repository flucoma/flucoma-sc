FluidAmpSlice : FluidRTUGen {
	*ar { arg in = 0, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85;

		^this.multiNew('audio', in.asAudioRateInput(this), fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq)
	}
	checkInputs {
		^this.checkValidInputs;
	}
}
