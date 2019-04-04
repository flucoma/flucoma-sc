FluidBufSines{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines, residual, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines.asUGenInput;
		residual = residual.asUGenInput;

		if(source.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		sines = sines ? -1;
		residual = residual ? -1;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufSines, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, winSize, hopSize, fftSize, maxFFTSize);
			server.sync;
			if (sines != -1) {sines = server.cachedBufferAt(sines); sines.updateInfo; server.sync;} {sines = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(sines, residual);
		};
	}
}
