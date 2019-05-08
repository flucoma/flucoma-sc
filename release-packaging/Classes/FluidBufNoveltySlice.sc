FluidBufNoveltySlice{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, kernSize = 3, threshold = 0.8, filterSize = 1, winSize = 1024, hopSize = -1, fftSize = -1, action;

		//var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize}; //ready for when we need it from the RT wrapper

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufNoveltySlice, source, startFrame, numFrames, startChan, numChans, indices, kernSize, threshold, filterSize, winSize, hopSize, fftSize);
			server.sync;
			indices = server.cachedBufferAt(indices); indices.updateInfo; server.sync;
			action.value(indices);
		};
	}
}
