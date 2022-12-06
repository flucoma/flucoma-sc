FluidBufScale : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, clipping = 0,  trig = 1, blocking = 1|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^FluidProxyUgen.kr(\FluidBufScaleTrigger, -1, source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh, clipping, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, clipping = 0,  freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh, clipping, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, clipping = 0,  freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh, clipping, 1], freeWhenDone, action
		);
	}
}
FluidBufScaleTrigger : FluidProxyUgen {}
