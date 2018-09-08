FDTransients {
	 *process { arg server, srcBuf, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, transBuf, resBuf, order = 200, blockSize = 2048, padding = 1024, skew = 0, threshFwd = 3, threshBack = 1.1, windowSize = 14, debounce = 25;

		var transBufNum,resBufNum;

		if(srcBuf.bufnum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		transBufNum = if(transBuf.isNil, -1, {transBuf.bufnum});
		resBufNum =  if(resBuf.isNil, -1, {resBuf.bufnum});

		server.sendMsg(\cmd, \BufTransient,
			srcBuf.bufnum, startAt, nFrames, startChan, nChans, transBufNum, resBufNum, order, blockSize, padding, skew, threshBack, windowSize, debounce);
	}
}
