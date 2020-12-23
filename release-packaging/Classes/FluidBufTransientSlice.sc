FluidBufTransientSlice : FluidBufProcessor {

    *objectClassName{^\FluidBufTrSlice}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, trig = 1, blocking = 0|

        source = source.asUGenInput;
        indices = indices.asUGenInput;

        source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
        indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(this.objectClassName++\Trigger, -1, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength, trig, blocking);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, freeWhenDone = true, action|

        source = source.asUGenInput;
        indices = indices.asUGenInput;

        source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
        indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

        ^this.new(
			server, nil,[indices]
        ).processList([source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength,0], freeWhenDone, action
        );
    }

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, freeWhenDone = true, action|

        source = source.asUGenInput;
        indices = indices.asUGenInput;

        source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
        indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

        ^this.new(
			server, nil,[indices]
        ).processList([source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength,1], freeWhenDone, action
        );
    }
}
