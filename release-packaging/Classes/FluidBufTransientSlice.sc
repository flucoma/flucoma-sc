FluidBufTransientSlice{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, action;

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufTransientSlice, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength);
			server.sync;
			indices = server.cachedBufferAt(indices); indices.updateInfo; server.sync;
			action.value(indices);
		};
	}
}
