FluidBufTransients {
	 *process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, transBufNum, resBufNum, order = 200, blockSize = 2048, padding = 1024, skew = 0, threshFwd = 3, threshBack = 1.1, windowSize = 14, debounce = 25;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		transBufNum = transBufNum ? -1;
		resBufNum = resBufNum ? -1;

		server.sendMsg(\cmd, \BufTransients, srcBufNum, startAt, nFrames, startChan, nChans, transBufNum, resBufNum, order, blockSize, padding, skew, threshFwd, threshBack, windowSize, debounce);
	}
}