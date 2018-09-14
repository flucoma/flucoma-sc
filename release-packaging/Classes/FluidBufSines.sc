FluidBufSines{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, sineBufNum, resBufNum, bandwidth = 76, threshold = 0.3, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, winSize = 4096, hopSize = 1024, fftSize = 8192;

		if(srcBufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		sineBufNum = sineBufNum ? -1;
		resBufNum = resBufNum ? -1;

		server.sendMsg(\cmd, \BufSines, srcBufNum, startAt, nFrames, startChan, nChans, sineBufNum, resBufNum, bandwidth, threshold, minTrackLen, magWeight, freqWeight, winSize, hopSize, fftSize);
}
}