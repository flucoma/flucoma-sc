FluidBufTransientSlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, transBufNum, order = 200, blockSize = 2048, padSize = 1024, skew = 0, threshFwd = 3, threshBack = 1.1, winSize = 14, debounce = 25, minSlice = 1000;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(transBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		server.sendMsg(\cmd, \BufTransientSlice, srcBufNum, startAt, nFrames, startChan, nChans, transBufNum, order, blockSize, padSize, skew, threshFwd, threshBack, winSize, debounce, minSlice);
	}
}
