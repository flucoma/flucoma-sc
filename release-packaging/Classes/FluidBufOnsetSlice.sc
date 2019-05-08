FluidBufOnsetSlice{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, function = 0, threshold = 0.1, debounce = 2, filterSize = 5, frameDelta = 0, winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufOnsetSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufOnsetSlice:  Invalid features buffer".throw};

		server = server ? Server.default;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufOnsetSlice, source, startFrame, numFrames, startChan, numChans, indices, function, threshold, debounce, filterSize, frameDelta, winSize, hopSize, fftSize, maxFFTSize);
			server.sync;
			indices = server.cachedBufferAt(indices); indices.updateInfo; server.sync;
			action.value(indices);
		};
	}
}
