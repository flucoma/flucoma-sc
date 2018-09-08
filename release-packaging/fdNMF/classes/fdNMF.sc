FDNMF {
	*process { arg server,  srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, dstBufNum, dictBufNum, dictFlag = 0, actBufNum, actFlag = 0, rank = 1, iterations = 100, sortFlag = 0, windowSize = 1024, hopSize = 256, fftSize = -1, windowType = 0, randomSeed = -1;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		dstBufNum = dstBufNum ? -1;
		dictBufNum = dictBufNum ? -1;
		actBufNum = actBufNum ? -1;

		server.sendMsg(\cmd, \BufNMF, srcBufNum, startAt, nFrames, startChan, nChans, dstBufNum, dictBufNum, dictFlag, actBufNum, actFlag, rank, iterations, windowSize, hopSize,fftSize);
	}
}