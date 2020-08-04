FluidBufStats : UGen{

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, trig = 1, blocking = 0|

		source = source.asUGenInput;
		stats = stats.asUGenInput;

		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, stats, numDerivs, low, middle, high,trig, blocking);
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, trig = 1, blocking = 0| 
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, stats, numDerivs, low, middle, high,trig, blocking);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, action|
		^FluidNRTProcess.new(
			server, this, action, [stats]
		).process(
			source, startFrame, numFrames, startChan, numChans, stats,numDerivs, low, middle, high
		);
	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, action|
		^FluidNRTProcess.new(
			server, this, action, [stats], blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, stats,numDerivs, low, middle, high
		);
	}

}
