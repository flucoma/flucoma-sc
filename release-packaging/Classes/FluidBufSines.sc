FluidBufSines : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines !? {sines.asUGenInput} ?? {-1};
		residual = residual !? {residual.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		^FluidProxyUgen.multiNew(\FluidBufSinesTrigger, -1, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines !? {sines.asUGenInput} ?? {-1};
		residual = residual !? {residual.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		^this.new(
			server, nil, [sines, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize,maxFFTSize,0],freeWhenDone = true,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines !? {sines.asUGenInput} ?? {-1};
		residual = residual !? {residual.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		^this.new(
			server, nil, [sines, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize,maxFFTSize,1],freeWhenDone,action
		);
	}

}
FluidBufSinesTrigger : FluidProxyUgen {}
