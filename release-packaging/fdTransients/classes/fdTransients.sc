FDTransients {
	 *process { arg server, srcBuf, offsetframes = 0, numframes = -1, offsetchans = 0, numchans = -1, transbuf, resbuf, order = 200, blocksize = 2048, padding = 1024, skew = 0, threshfwd = 3, threshback = 1.1, windowsize = 14, debounce = 25;
		server = server ? Server.default;

		if(srcBuf.bufnum.isNil) { Error("Invalid buffer").format(thisMethod.name, this.class.name).throw};

		server.sendMsg(\cmd, \BufNMF,
			srcBuf.bufnum, offsetframes, numframes, offsetchans, numchans,
			if(transbuf.isNil, -1, {transbuf.bufnum}),
			if(resbuf.isNil, -1, {resbuf.bufnum}),
			order,blocksize,padding,skew,threshback,windowsize,debounce
			);
	}
}
