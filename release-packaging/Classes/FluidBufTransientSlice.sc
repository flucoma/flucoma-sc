FluidBufTransientSlice{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, winSize = 14, debounce = 25, minSlice = 1000, action;

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		if(source.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(indices.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufTransientSlice, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, winSize, debounce, minSlice);
			server.sync;
			indices = server.cachedBufferAt(indices); indices.updateInfo; server.sync;
			action.value(indices);
		};
	}
}
