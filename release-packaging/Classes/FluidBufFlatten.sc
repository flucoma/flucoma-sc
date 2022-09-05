FluidBufFlatten : FluidBufProcessor {


	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, trig = 1, blocking = 1|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^FluidProxyUgen.kr(\FluidBufFlattenTrigger,-1,  source, startFrame, numFrames, startChan, numChans, destination, axis, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, axis,0],freeWhenDone,action
		);

	}

	*processBlocking  { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, axis = 1, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, axis,1],freeWhenDone,action
		);

	}
}
FluidBufFlattenTrigger : FluidProxyUgen {}
