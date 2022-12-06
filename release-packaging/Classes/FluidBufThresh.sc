FluidBufThresh : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, threshold = 0, trig = 1, blocking = 1|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^FluidProxyUgen.kr(\FluidBufThreshTrigger, -1, source, startFrame, numFrames, startChan, numChans, destination, threshold, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, freeWhenDone = true,  action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, threshold, 0], freeWhenDone, action
		);

	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, threshold, 1], freeWhenDone, action
		);

	}
}
FluidBufThreshTrigger : FluidProxyUgen {}
