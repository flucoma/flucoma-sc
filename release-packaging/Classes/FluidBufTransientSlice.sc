FluidBufTransientSlice{
		*process { arg server, src, offsetframes = 0, numframes = -1, offsetchans = 0, numchans = -1, transbuf, order = 200, blocksize = 2048, padding = 1024, skew = 0, threshfwd = 3, threshback = 1.1, windowsize = 14, debounce = 25;

		server = server ? Server.default
;if(src.bufnum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

			server.sendMsg(\cmd, \BufTransientSlice, src.bufnum, offsetframes, numframes, offsetchans, numchans,
if( transbuf.isNil, -1, {transbuf.bufnum}), order, blocksize, padding, skew, threshfwd, threshback, windowsize, debounce);



}
}