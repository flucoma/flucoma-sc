FluidBufNoveltySlice{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, transBufNum, kernelSize = 3, threshold = 0.8, winSize = 1024, hopSize = 512, fftSize = 2048;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};
		if(transBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		server.sendMsg(\cmd, \BufNoveltySlice, srcBufNum, startAt, nFrames, startChan, nChans, transBufNum, kernelSize, threshold, winSize, hopSize, fftSize);
	}
}
