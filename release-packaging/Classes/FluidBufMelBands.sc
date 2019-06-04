FluidBufMelBands{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufMelBands:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufMelBands:  Invalid features buffer".throw};

		server = server ? Server.default;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)
		// same for maxNumBands which is passed numBands

		forkIfNeeded{
			server.sendMsg(\cmd, \BufMelBands, source, startFrame, numFrames, startChan, numChans, features, numBands, minFreq, maxFreq, numBands, windowSize, hopSize, fftSize, maxFFTSize);
			server.sync;
			features = server.cachedBufferAt(features); features.updateInfo; server.sync;
			action.value(features);
		};
	}
}
