FluidBufNoveltySlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, indBufNum, kernSize = 3, thresh = 0.8, filtSize = 1, winSize = 1024, hopSize = -1, fftSize = -1, action;

		//var maxFFTSize = if (fftSize == -1) {winSize.nextPowerOfTwo} {fftSize}; //ready for when we need it from the RT wrapper

		srcBufNum = srcBufNum.asUGenInput;
		indBufNum = indBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(indBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufNoveltySlice, srcBufNum, startAt, nFrames, startChan, nChans, indBufNum, kernSize, thresh, filtSize, winSize, hopSize, fftSize);
			server.sync;
			indBufNum = server.cachedBufferAt(indBufNum); indBufNum.updateInfo; server.sync;
			action.value(indBufNum);
		};
	}
}
