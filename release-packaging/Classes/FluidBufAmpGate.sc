FluidBufAmpGate : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, trig = 1, blocking = 0|

		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		^FluidProxyUgen.kr(\FluidBufAmpGateTrigger,-1, source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq,maxSize,  trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, freeWhenDone = true, action |


		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source ? -1;
		indices = indices ? -1;

		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq, maxSize, 0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, rampUp = 10, rampDown = 10, onThreshold = -90, offThreshold = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, highPassFreq = 85, freeWhenDone = true, action |


		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source ? -1;
		indices = indices ? -1;

		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, rampUp, rampDown, onThreshold, offThreshold, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, highPassFreq, maxSize, 1],freeWhenDone,action
		);
	}

}
FluidBufAmpGateTrigger : FluidProxyUgen {}
