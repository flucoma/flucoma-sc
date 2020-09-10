FluidBufStats : UGen{

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, weightsThreshold = 0, trig = 1, blocking = 0|

		source = source.asUGenInput;
		stats = stats.asUGenInput;
        weights = weights.asUGenInput;

		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};
		weights = weights ? -1;

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, stats, numDerivs, low, middle, high, outliersCutoff, weights, weightsThreshold, trig, blocking);
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, weightsThreshold = 0, trig = 1, blocking = 0|
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, stats, numDerivs, low, middle, high, outliersCutoff, weights, weightsThreshold, trig, blocking);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, weightsThreshold = 0, action|
		^FluidNRTProcess.new(
			server, this, action, [stats]
		).process(
			source, startFrame, numFrames, startChan, numChans, stats,numDerivs, low, middle, high, outliersCutoff, weights, weightsThreshold
		);
	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, weightsThreshold = 0, action|
		^FluidNRTProcess.new(
			server, this, action, [stats], blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, stats,numDerivs, low, middle, high, outliersCutoff, weights, weightsThreshold
		);
	}

}
