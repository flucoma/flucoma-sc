FluidBufAmpSlice : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, trig = 1, blocking = 0|

		source = this.validateBuffer(source, "source");
		indices = this.validateBuffer(indices, "indices");

		^FluidProxyUgen.kr(\FluidBufAmpSliceTrigger, -1, source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq, trig, blocking);
	}

	*process { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, freeWhenDone = true, action |

		source = this.validateBuffer(source, "source");
		indices = this.validateBuffer(indices, "indices");

		^this.new(server, nil, [indices]).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq,0],freeWhenDone, action
		);
	}

	*processBlocking { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, freeWhenDone = true, action |

		source = this.validateBuffer(source, "source");
		indices = this.validateBuffer(indices, "indices");

		^this.new(server, nil, [indices]).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq,1],freeWhenDone, action
		);
	}
}
FluidBufAmpSliceTrigger : FluidProxyUgen {}
