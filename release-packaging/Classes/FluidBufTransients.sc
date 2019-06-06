FluidBufTransients {
	 *process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients, residual, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, action;

		source = source.asUGenInput;
		transients = transients.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

		server = server ? Server.default;
		transients = transients ? -1;
		residual = residual ? -1;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufTransients, source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength);
			server.sync;
			if (transients != -1) {transients = server.cachedBufferAt(transients); transients.updateInfo; server.sync;} {transients = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(transients, residual);
		};
	}
}
