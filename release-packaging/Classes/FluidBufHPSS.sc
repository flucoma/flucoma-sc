FluidBufHPSS{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic, percussive, residual, harmFilterSize = 17, percFilterSize = 31, maskingMode, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		harmonic = harmonic.asUGenInput;
		percussive = percussive.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};

		server = server ? Server.default;
		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufHPSS, source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, winSize, hopSize, fftSize, maxFFTSize, harmFilterSize, percFilterSize);
			server.sync;
			if (harmonic != -1) {harmonic = server.cachedBufferAt(harmonic); harmonic.updateInfo; server.sync;} {harmonic = nil};
			if (percussive != -1) {percussive = server.cachedBufferAt(percussive); percussive.updateInfo; server.sync;} {percussive = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(harmonic, percussive, residual);
		};
	}
}
