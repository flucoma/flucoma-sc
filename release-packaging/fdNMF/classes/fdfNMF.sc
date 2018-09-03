// adds an instance method to the Buffer class
FDNMF {
	/*fdNMF { arg dstBuf, dictBuf, actBuf, rank = 1, iterations = 100, fftSize = 2048, windowSize = 2048, hopSize = 512, action;
		var resp;

		if(bufnum.isNil) { Error("Cannot call % on a % that has been freed".format(thisMethod.name, this.class.name)).throw };

/*		// responder idea stolen from getToFloatArray
		resp = OSCFunc({ arg msg;
			if(msg[1]== '/b_gen' && msg[2]== bufnum, {
				resp.clear;
				action.value(bufnum);
			});
		}, '/done', server.addr);*/

		// 		server.listSendMsg([\b_gen, bufnum, "BufNMF", if(dstBuf.isNil, -1, {dstBuf.bufnum}), if(dictBuf.isNil, -1, {dictBuf.bufnum}), if(actBuf.isNil, -1, {actBuf.bufnum}), rank, iterations, fftSize, windowSize, hopSize])
		server.sendMsg(\cmd, \BufNMF, bufnum, if(dstBuf.isNil, -1, {dstBuf.bufnum}), if(dictBuf.isNil, -1, {dictBuf.bufnum}), if(actBuf.isNil, -1, {actBuf.bufnum}), rank, iterations, fftSize, windowSize, hopSize);

	}*/


	*nmf { arg server,  srcBuf, startAt = 0, nFrames = -1,startChan = 0,nChans = -1, dstBuf, dictBuf, dictFlag = 0, actBuf, actFlag = 0, rank = 1, iterations = 100, windowSize = 2048, hopSize = 512, fftSize = 2048,  action;
		var resp;

		server = server ? Server.default;

		if(srcBuf.bufnum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		dstBuf.bufnum.postln;

		server.sendMsg(\cmd, \BufNMF,
		if(srcBuf.isNil, -1, {srcBuf.bufnum}),
		startAt, nFrames, startChan, nChans,
		if(dstBuf.isNil, -1, {dstBuf.bufnum}),
		if(dictBuf.isNil, -1, {dictBuf.bufnum}),
		dictFlag,
		if(actBuf.isNil, -1, {actBuf.bufnum}),
		actFlag,
		rank, iterations, windowSize, hopSize,fftSize);
	}


}
