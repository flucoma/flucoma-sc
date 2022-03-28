FluidBufOnsetCurve : FluidBufProcessor {
    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, metric = 0, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufOnsetCurve:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufOnsetCurve:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufOnsetCurveTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, metric, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, metric = 0, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufOnsetCurve:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufOnsetCurve:  Invalid features buffer".throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding,  metric, filterSize, frameDelta, windowSize, hopSize, fftSize,maxFFTSize,0],freeWhenDone,action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, metric = 0, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1,padding = 1,  freeWhenDone = true, action|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufOnsetCurve:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufOnsetCurve:  Invalid features buffer".throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, metric, filterSize, frameDelta, windowSize, hopSize, fftSize,maxFFTSize,1],freeWhenDone,action
		);
	}
}
FluidBufOnsetCurveTrigger : FluidProxyUgen {}
