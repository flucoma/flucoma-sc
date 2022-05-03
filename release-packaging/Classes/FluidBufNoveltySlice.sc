FluidBufNoveltySlice : FluidBufProcessor {

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, algorithm = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1 , blocking = 0| 

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufNoveltySliceTrigger, -1, source, startFrame, numFrames, startChan, numChans, indices, algorithm, kernelSize, threshold, filterSize, minSliceLength, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize, trig, blocking);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, algorithm= 0, kernelSize = 3, threshold = 0.5, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action |
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        indices = indices.asUGenInput;

        source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
        indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, algorithm, kernelSize, threshold, filterSize, minSliceLength, windowSize, hopSize, fftSize,  maxFFTSize, kernelSize, filterSize,0],freeWhenDone,action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, algorithm= 0, kernelSize = 3, threshold = 0.5, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action |
        
        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

        source = source.asUGenInput;
        indices = indices.asUGenInput;

        source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
        indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};
        
		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices, algorithm, kernelSize, threshold, filterSize, minSliceLength, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize,1],freeWhenDone,action
		);
	}
}
FluidBufNoveltySliceTrigger : FluidProxyUgen {}
