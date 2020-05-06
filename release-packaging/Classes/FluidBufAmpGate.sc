FluidBufAmpGate : UGen {

	*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, trig = 1, blocking|
		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq, maxSize, trig, blocking);
	}

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, trig = 1|

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq,  trig,blocking:0);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, action |

		^FluidNRTProcess.new(
			server, this, action, [indices]
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq
        );
	}

     *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, action|

		^FluidNRTProcess.new(
			server, this, action, [indices], blocking: 1
		).process(
              source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq
		);
	}
}
