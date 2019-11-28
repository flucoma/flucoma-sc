FluidBufAmpSlice : UGen {

	*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, doneAction = 0, blocking|
		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, maxSize, 0, doneAction, blocking);
	}

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, doneAction = 0|

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, 0, doneAction,blocking:0);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, action |

		^FluidNRTProcess.new(
			server, this, action, [indices]
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq
		);
	}

     *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, action|

		^FluidNRTProcess.new(
			server, this, action, [indices], blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq
		);
	}
}
