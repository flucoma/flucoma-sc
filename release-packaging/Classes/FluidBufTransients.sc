FluidBufTransients {
	 *process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, transBufNum, resBufNum, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, winSize = 14, debounce = 25;

		srcBufNum = srcBufNum.asUGenInput;
		transBufNum = transBufNum.asUGenInput;
		resBufNum = resBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		transBufNum = transBufNum ? -1;
		resBufNum = resBufNum ? -1;

		server.sendMsg(\cmd, \BufTransients, srcBufNum, startAt, nFrames, startChan, nChans, transBufNum, resBufNum, order, blockSize, padSize, skew, threshFwd, threshBack, winSize, debounce);
	}
}
