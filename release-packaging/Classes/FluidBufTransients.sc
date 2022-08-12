FluidBufTransients : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, trig = 1, blocking = 0|

		source = source.asUGenInput;
		transients = transients ? -1;
		residual = residual ? -1;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

		^FluidProxyUgen.kr(\FluidBufTransientsTrigger, -1, source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, freeWhenDone = true, action|

		source = source.asUGenInput;
		transients = transients ? -1;
		residual = residual ? -1;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

		^this.new(
			server, nil,[transients, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, freeWhenDone = true, action|

		source = source.asUGenInput;
		transients = transients ? -1;
		residual = residual ? -1;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

		^this.new(
			server, nil,[transients, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength,1],freeWhenDone = true,action
		);
	}
}
FluidBufTransientsTrigger : FluidProxyUgen {}
