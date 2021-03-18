FluidBufPitch : FluidBufProcessor{
    
	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
            
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};    
            
        ^FluidProxyUgen.kr(\FluidBufPitchTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, 0], freeWhenDone, action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, 1], freeWhenDone, action
		);
	}
}
FluidBufPitchTrigger : FluidProxyUgen {}
