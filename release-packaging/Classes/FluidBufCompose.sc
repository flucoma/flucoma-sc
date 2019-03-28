FluidBufCompose{
		*process { arg server, srcBufNum, startAt = 0, nFrames = -1, startChan = 0, nChans = -1, srcGain = 1, dstBufNum, dstStartAt = 0, dstStartChan = 0, dstGain = 0;

		srcBufNum = srcBufNum.asUGenInput;
		dstBufNum = dstBufNum.asUGenInput;

		if(srcBufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};
		if(dstBufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;
		server.sendMsg(\cmd, \BufCompose, srcBufNum, startAt, nFrames, startChan, nChans, srcGain, dstBufNum, dstStartAt, dstStartChan, dstGain);
	}
}
