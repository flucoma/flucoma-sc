FluidBufNMF {
	*process { arg server,  srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, dstBufNum, dictBufNum, dictFlag = 0, actBufNum, actFlag = 0, rank = 1, nIter = 100, sortFlag = 0, winSize = 1024, hopSize = -1, fftSize = -1, winType = 0, randSeed = -1;


		srcBufNum = srcBufNum.asUGenInput;
		dstBufNum = dstBufNum.asUGenInput;
		dictBufNum = dictBufNum.asUGenInput;
		actBufNum = actBufNum.asUGenInput;

		if(srcBufNum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		dstBufNum = dstBufNum ? -1;
		dictBufNum = dictBufNum ? -1;
		actBufNum = actBufNum ? -1;

		server.sendMsg(\cmd, \BufNMF, srcBufNum, startAt, nFrames, startChan, nChans, dstBufNum, dictBufNum, dictFlag, actBufNum, actFlag, rank, nIter, winSize, hopSize, fftSize);
	}
}
