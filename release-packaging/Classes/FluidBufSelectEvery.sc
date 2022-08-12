FluidBufSelectEvery : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, frameHop = 1, chanHop = 1, trig = 1, blocking = 1|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufSelectEvery:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelectEvery:  Invalid destination buffer".throw};

		^FluidProxyUgen.kr(\FluidBufSelectEveryTrigger, -1, source, startFrame, numFrames, startChan, numChans, destination, frameHop, chanHop, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, frameHop = 1, chanHop = 1, freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufSelectEvery:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelectEvery:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, frameHop, chanHop, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, frameHop = 1, chanHop = 1, freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufSelectEvery:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelectEvery:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, frameHop, chanHop, 1], freeWhenDone, action
		);
	}
}
FluidBufSelectEveryTrigger : FluidProxyUgen {}
