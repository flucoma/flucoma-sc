// adds an instance method to the Buffer class
+ Buffer {
	fdNMF { arg dstBuf, repetitions = 0;
		if(bufnum.isNil) { Error("Cannot call % on a % that has been freed".format(thisMethod.name, this.class.name)).throw };
		server.listSendMsg([\b_gen, dstBuf.bufnum, "BufNMF", bufnum, repetitions])
	}
}
