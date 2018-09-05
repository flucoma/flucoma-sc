FDNMF {
	*nmf { arg server,  srcBuf, startAt = 0, nFrames = -1,startChan = 0,nChans = -1, dstBuf, dictBuf, dictFlag = 0, actBuf, actFlag = 0, rank = 1, iterations = 100, sortFlag = 0, windowSize = 2048, hopSize = 512, fftSize = -1 ;
		var resp;

		server = server ? Server.default;

		if(srcBuf.bufnum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server.sendMsg(\cmd, \BufNMF,
			srcBuf.bufnum, startAt, nFrames, startChan, nChans,
			if(dstBuf.isNil, -1, {dstBuf.bufnum}),
			if(dictBuf.isNil, -1, {dictBuf.bufnum}),
			dictFlag,
			if(actBuf.isNil, -1, {actBuf.bufnum}),
			actFlag, rank, iterations, windowSize, hopSize,fftSize);
	}
}
