FluidAmpGate : FluidRTUGen {
	*ar { arg in = 0, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, maxSize = 88200;
		^this.multiNew('audio', in.asAudioRateInput(this), rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq, maxSize)
	}
	checkInputs {
		if(inputs.at(12).rate != 'scalar') {
			^(": maxSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
