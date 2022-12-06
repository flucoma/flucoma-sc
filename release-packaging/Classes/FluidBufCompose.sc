FluidBufCompose : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, trig = 1, blocking = 1|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^FluidProxyUgen.kr(\FluidBufComposeTrigger,-1, source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, trig, blocking);
	}


	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new( server, nil, [destination]).processList([source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, 1], freeWhenDone, action);//NB always blocking
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		destination = this.validateBuffer(destination, "destination");

		^this.new(
			server, nil, [destination]
		).processList([source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, 1], freeWhenDone, action);
	}
}
FluidBufComposeTrigger : FluidProxyUgen {}
