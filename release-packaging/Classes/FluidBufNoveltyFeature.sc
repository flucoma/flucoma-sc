FluidBufNoveltyFeature : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, algorithm = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;
		algorithm = FluidNoveltySlice.prSelectAlgorithm(algorithm) ?? {
			("FluidBufNoveltySlice: % is not a recognised algorithm")
			.format(algorithm).throw;
		};

		source.isNil.if {"FluidBufNoveltyFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufNoveltyFeature:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufNoveltyFeatureTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, algorithm, kernelSize, kernelSize, filterSize, filterSize, windowSize, hopSize, fftSize, maxFFTSize,  trig, blocking);

	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, algorithm= 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;
		algorithm = FluidNoveltySlice.prSelectAlgorithm(algorithm);
		if (algorithm.isNil or: algorithm.isUGen) {
			("FluidBufNoveltySlice: % is not a recognised algorithm")
			.format(algorithm).throw;
		};

		source.isNil.if {"FluidBufNoveltyFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufNoveltyFeature:  Invalid features buffer".throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, algorithm, kernelSize, kernelSize, filterSize, filterSize, windowSize, hopSize, fftSize,  maxFFTSize, 0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, algorithm= 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufNoveltyFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufNoveltyFeature:  Invalid features buffer".throw};
		algorithm = FluidNoveltySlice.prSelectAlgorithm(algorithm);
		if (algorithm.isNil or: algorithm.isUGen) {
			("FluidBufNoveltySlice: % is not a recognised algorithm")
			.format(algorithm).throw;
		};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, algorithm, kernelSize, kernelSize, filterSize, filterSize, windowSize, hopSize, fftSize, maxFFTSize, 1],freeWhenDone,action
		);
	}
}
FluidBufNoveltyFeatureTrigger : FluidProxyUgen {}
