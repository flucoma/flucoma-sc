FluidBufNoveltyCurve : FluidBufProcessor {

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, feature = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0| 

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufNoveltyCurve:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufNoveltyCurve:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufNoveltyCurveTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, feature, kernelSize, filterSize, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize, trig, blocking);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, feature = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action |
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufNoveltyCurve:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufNoveltyCurve:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, feature, kernelSize, filterSize, windowSize, hopSize, fftSize,  maxFFTSize, kernelSize, filterSize,0],freeWhenDone,action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, feature = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action |
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufNoveltyCurve:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufNoveltyCurve:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, feature, kernelSize, filterSize, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize,1],freeWhenDone,action
		);
	}
}
FluidBufNoveltyCurveTrigger : FluidProxyUgen {}
