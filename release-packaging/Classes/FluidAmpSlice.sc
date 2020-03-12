FluidAmpSlice : UGen {
	*ar { arg in = 0, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, highPassFreq = 85, minSliceLength = 2;

		^this.multiNew('audio', in.asAudioRateInput(this), fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, highPassFreq, minSliceLength)
	}
	checkInputs {
		^this.checkValidInputs;
	}
}
