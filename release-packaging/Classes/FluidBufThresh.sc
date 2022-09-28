FluidBufThresh : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, threshold = 0, trig = 1, blocking = 1|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};

		^FluidProxyUgen.kr(\FluidBufThreshTrigger, -1, source, startFrame, numFrames, startChan, numChans, destination, threshold, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, freeWhenDone = true,  action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, threshold, 0], freeWhenDone, action
		);

	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, threshold, 1], freeWhenDone, action
		);

	}
}
FluidBufThreshTrigger : FluidProxyUgen {}
