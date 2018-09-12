FluidBufHPSS{
		*process { arg server, src, offsetframes = 0, numframes = -1, offsetchans = 0, numchans = -1, harmbuf, percbuf, psize = 31, hsize = 17, winsize = 4096, hopsize = 1024, fftsize = -1;

		server = server ? Server.default;

		if(src.bufnum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

			server.sendMsg(\cmd, \BufHPSS, src.bufnum, offsetframes, numframes, offsetchans, numchans,
if( harmbuf.isNil, -1, {harmbuf.bufnum}),
if( percbuf.isNil, -1, {percbuf.bufnum}), psize, hsize, winsize, hopsize, fftsize);



}
}