FluidBufOnsetSlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, idxBufNum, function = 0, threshold = 0.1, debounce = 2, filterSize = 5, frameDelta = 0, winSize = 1024, hopSize = 256, fftSize = 1024, maxFFTSize = 16384;

		if(srcBufNum.isNil) {Error("Invalid Buffer % %".format(this.class.name, thisMethod.name)).throw};

		server = server ? Server.default;
		idxBufNum = idxBufNum ? -1;


		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		server.sendMsg(\cmd, \BufOnsetSlice, srcBufNum, startAt, nFrames, startChan, nChans, idxBufNum, function, threshold, debounce, filterSize, frameDelta, winSize, hopSize, fftSize, maxFFTSize);
}
}
