FluidBufHPSS{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, harmBufNum, percBufNum, pSize = 31, hSize = 17, winSize = 4096, hopSize = 1024, fftSize = -1;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		harmBufNum = harmBufNum ? -1;
		percBufNum = percBufNum ? -1;

		server.sendMsg(\cmd, \BufHPSS, srcBufNum, startAt, nFrames, startChan, nChans, harmBufNum, percBufNum, pSize, hSize, winSize, hopSize, fftSize);
}
}
