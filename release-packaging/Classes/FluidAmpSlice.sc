FluidAmpSlice : UGen {
	*ar { arg in = 0, absRampUp = 10, absRampDown = 10, absThreshOn = -40, absThreshOff = -40, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = -144, relThreshOff = -144, highPassFreq = 250, maxSize = 88200, outputType = 0;
		^this.multiNew('audio', in.asAudioRateInput(this), absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, maxSize, outputType)
	}
	checkInputs {
		if(inputs.at(16).rate != 'scalar') {
			^(": maxSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
