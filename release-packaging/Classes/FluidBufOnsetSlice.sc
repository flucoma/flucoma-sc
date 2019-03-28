FluidBufOnsetSlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, indBufNum, function = 0, threshold = 0.1, debounce = 2, filterSize = 5, frameDelta = 0, winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		srcBufNum = srcBufNum.asUGenInput;
		indBufNum = indBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(indBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufOnsetSlice, srcBufNum, startAt, nFrames, startChan, nChans, indBufNum, function, threshold, debounce, filterSize, frameDelta, winSize, hopSize, fftSize, maxFFTSize);
			server.sync;
			indBufNum = server.cachedBufferAt(indBufNum); indBufNum.updateInfo; server.sync;
			action.value(indBufNum);
		};
	}
}
