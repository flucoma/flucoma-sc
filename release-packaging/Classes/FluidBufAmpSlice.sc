FluidBufAmpSlice : UGen {

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, doneAction = 0|
		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, maxSize, 0, doneAction);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, action|

        var instance,synth;

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^FluidNRTProcess.new(
			server, this, action, [indices]
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq
		);
	}
}
