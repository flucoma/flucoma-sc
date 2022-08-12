FluidBufChroma : FluidBufProcessor {
	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, numChroma = 12, ref = 440, normalize = 0,minFreq = 0,maxFreq = -1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufChroma:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufChroma:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufChromaTrigger,-1, source, startFrame, numFrames, startChan, numChans, features, padding, numChroma, numChroma, ref, normalize, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numChroma = 12, ref = 440, normalize = 0,minFreq = 0,maxFreq = -1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufChroma:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufChroma:  Invalid features buffer".throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numChroma, numChroma, ref, normalize, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize, 0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numChroma = 12, ref = 440, normalize = 0,minFreq = 0,maxFreq = -1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufChroma:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufChroma:  Invalid features buffer".throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numChroma, numChroma, ref, normalize, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize, 1],freeWhenDone,action
		);
	}
}
FluidBufChromaTrigger : FluidProxyUgen {}
