FluidBufSines{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, sineBufNum, resBufNum, bw = 76, thresh = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, winSize = 1024, hopSize = -1, fftSize = -1;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		srcBufNum = srcBufNum.asUGenInput;
		sineBufNum = sineBufNum.asUGenInput;
		resBufNum = resBufNum.asUGenInput;

		if(srcBufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		sineBufNum = sineBufNum ? -1;
		resBufNum = resBufNum ? -1;


		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		server.sendMsg(\cmd, \BufSines, srcBufNum, startAt, nFrames, startChan, nChans, sineBufNum, resBufNum, bw, thresh, minTrackLen, magWeight, freqWeight, winSize, hopSize, fftSize, maxFFTSize);
}
}
