// adds an instance method to the Buffer class
+ Buffer {
	fdNMF { arg dstBuf, rank = 1, fftSize = 2048, windowSize = 2048, hopSize = 512;
		if(bufnum.isNil) { Error("Cannot call % on a % that has been freed".format(thisMethod.name, this.class.name)).throw };
		server.listSendMsg([\b_gen, dstBuf.bufnum, "BufNMF", bufnum, rank, fftSize, windowSize, hopSize])
	}
}
