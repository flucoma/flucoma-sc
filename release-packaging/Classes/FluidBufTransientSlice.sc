FluidBufTransientSlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, indBufNum, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, winSize = 14, debounce = 25, minSlice = 1000, action;

		srcBufNum = srcBufNum.asUGenInput;
		indBufNum = indBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(indBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufTransientSlice, srcBufNum, startAt, nFrames, startChan, nChans, indBufNum, order, blockSize, padSize, skew, threshFwd, threshBack, winSize, debounce, minSlice);
			server.sync;
			indBufNum = server.cachedBufferAt(indBufNum); indBufNum.updateInfo; server.sync;
			action.value(indBufNum);
		};
	}
}
