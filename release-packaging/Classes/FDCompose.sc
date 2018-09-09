FDCompose{
		*process { arg server, srcBufNumA, startAtA = 0, nFramesA = -1, startChanA = 0, nChansA = -1, srcGainA = 1, dstStartAtA = 0, dstStartChanA = 0, srcBufNumB, startAtB = 0, nFramesB = -1, startChanB = 0, nChansB = -1, srcGainB = 1, dstStartAtB = 0, dstStartChanB = 0, dstBufNum;

		if(srcBufNumA.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};
		if(srcBufNumB.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};
		if(dstBufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		server.sendMsg(\cmd, \BufCompose, srcBufNumA, startAtA, nFramesA, startChanA, nChansA, srcGainA, dstStartAtA, dstStartChanA,
			srcBufNumB, startAtB, nFramesB, startChanB, nChansB, srcGainB, dstStartAtB, dstStartChanB,
			dstBufNum);
	}
}