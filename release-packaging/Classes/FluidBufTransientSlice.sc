FluidBufTransientSlice : UGen{
    	*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, doneAction = 0, blocking = 0|

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength, doneAction, blocking);
	}

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, doneAction = 0|
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength, doneAction);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, action|
        ^FluidNRTProcess.new(
			server, this, action,[indices]
        ).process(source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength
        );
    }

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, action|
        ^FluidNRTProcess.new(
			server, this, action,[indices], blocking: 1
        ).process(source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength
        );
    }
}
