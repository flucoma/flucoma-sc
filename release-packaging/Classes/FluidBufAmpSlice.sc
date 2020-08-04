FluidBufAmpSlice : UGen {

	*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, trig = 1, blocking|

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq, trig, blocking);
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, trig = 1, blocking = 0| 
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq, trig, blocking);
	}

    *process { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, action |

		^FluidNRTProcess.new(
			server, this, action, [indices]
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq
		);
	}

     *processBlocking { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, onThreshold = -144, offThreshold = -144, floor = -144, minSliceLength = 2, highPassFreq = 85, action|

		^FluidNRTProcess.new(
			server, this, action, [indices], blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, fastRampUp, fastRampDown, slowRampUp, slowRampDown, onThreshold, offThreshold, floor, minSliceLength, highPassFreq
		);
	}
}
