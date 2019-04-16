FluidBufSpectralShape{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufSpectralShape:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufSpectralShape:  Invalid features buffer".throw};

		server = server ? Server.default;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufSpectralShape, source, startFrame, numFrames, startChan, numChans, features, winSize, hopSize, fftSize, maxFFTSize);
			server.sync;
			features = server.cachedBufferAt(features); features.updateInfo; server.sync;
			action.value(features);
		};
	}
}
