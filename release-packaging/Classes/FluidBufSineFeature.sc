FluidBufSineFeature : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, frequency = -1, magnitude = -1, numPeaks = 10, detectionThreshold = -96, order = 0, freqUnit = 0, magUnit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		frequency = frequency !? {frequency.asUGenInput} ?? {-1};
		magnitude = magnitude !? {magnitude.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSineFeature:  Invalid source buffer".throw};

		^FluidProxyUgen.multiNew(\FluidBufSineFeatureTrigger, -1, source, startFrame, numFrames, startChan, numChans, frequency, magnitude, padding, numPeaks, numPeaks, detectionThreshold, order, freqUnit, magUnit, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, frequency = -1, magnitude = -1, numPeaks = 10, detectionThreshold = -96, order = 0, freqUnit = 0, magUnit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		frequency = frequency !? {frequency.asUGenInput} ?? {-1};
		magnitude = magnitude !? {magnitude.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSineFeature:  Invalid source buffer".throw};

		^this.new(
			server, nil, [frequency, magnitude].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, frequency, magnitude, padding, numPeaks, numPeaks, detectionThreshold, order, freqUnit, magUnit, windowSize, hopSize, fftSize,maxFFTSize,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, frequency = -1, magnitude = -1, numPeaks = 10, detectionThreshold = -96, order = 0, freqUnit = 0, magUnit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		frequency = frequency !? {frequency.asUGenInput} ?? {-1};
		magnitude = magnitude !? {magnitude.asUGenInput} ?? {-1};

		source.isNil.if {"FluidBufSineFeature:  Invalid source buffer".throw};

		^this.new(
			server, nil, [frequency, magnitude].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, frequency, magnitude, padding, numPeaks, numPeaks, detectionThreshold, order, freqUnit, magUnit, windowSize, hopSize, fftSize,maxFFTSize,1],freeWhenDone,action
		);
	}

}
FluidBufSineFeatureTrigger : FluidProxyUgen {}