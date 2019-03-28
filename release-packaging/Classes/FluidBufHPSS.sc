FluidBufHPSS{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, harmBufNum, percBufNum, resBufNum, hFiltSize = 17, pFiltSize = 31, modeFlag, htf1 = 0.1, hta1 = 0, htf2 = 0.5, hta2 = 0, ptf1 = 0.1, pta1 = 0, ptf2 = 0.5, pta2 = 0, winSize = 1024, hopSize = -1, fftSize = -1, action;

		var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize};

		srcBufNum = srcBufNum.asUGenInput;
		harmBufNum = harmBufNum.asUGenInput;
		percBufNum = percBufNum.asUGenInput;
		resBufNum = resBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		harmBufNum = harmBufNum ? -1;
		percBufNum = percBufNum ? -1;
		resBufNum = resBufNum ? -1;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		forkIfNeeded{
			server.sendMsg(\cmd, \BufHPSS, srcBufNum, startAt, nFrames, startChan, nChans, harmBufNum, percBufNum, resBufNum, hFiltSize, pFiltSize, modeFlag, htf1, hta1, htf2, hta2, ptf1, pta1, ptf2, pta2, winSize, hopSize, fftSize, maxFFTSize, hFiltSize, pFiltSize);
			server.sync;
			if (harmBufNum != -1) {harmBufNum = server.cachedBufferAt(harmBufNum); harmBufNum.updateInfo; server.sync;} {harmBufNum = nil};
			if (percBufNum != -1) {percBufNum = server.cachedBufferAt(percBufNum); percBufNum.updateInfo; server.sync;} {percBufNum = nil};
			if (resBufNum != -1) {resBufNum = server.cachedBufferAt(resBufNum); resBufNum.updateInfo;server.sync;} {resBufNum = nil};
			action.value(harmBufNum, percBufNum, resBufNum);
		};
	}
}
