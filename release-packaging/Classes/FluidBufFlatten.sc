FluidBufFlatten : FluidBufProcessor {


	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, trig = 1, blocking = 1|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};

		^FluidProxyUgen.kr(\FluidBufFlattenTrigger,-1,  source, startFrame, numFrames, startChan, numChans, destination, axis, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, axis,0],freeWhenDone,action
		);

	}

	*processBlocking  { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, axis,1],freeWhenDone,action
		);

	}
}
FluidBufFlattenTrigger : FluidProxyUgen {}
