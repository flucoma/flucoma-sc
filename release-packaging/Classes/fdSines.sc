FDSines{
		*process { arg server, src, offsetframes = 0, numframes = -1, offsetchans = 0, numchans = -1, sinebuf, resbuf, bandwidth = 76, threshold = 0.7, mintracklen = 15, magweight = 0.1, freqweight = 1, winsize = 4096, hopsize = 1024, fftsize = -1;

		server = server ? Server.default;
		if(src.bufnum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

			server.sendMsg(\cmd, \BufSines, src.bufnum, offsetframes, numframes, offsetchans, numchans,
if( sinebuf.isNil, -1, {sinebuf.bufnum}),
if( resbuf.isNil, -1, {resbuf.bufnum}), bandwidth, threshold, mintracklen, magweight, freqweight, winsize, hopsize, fftsize);



}
}