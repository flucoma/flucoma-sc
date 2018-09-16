FluidBufNoveltySlice{
		*process { arg server, src, offsetframes = 0, numframes = -1, offsetchans = 0, numchans = -1, transbuf, kernelsize = 3, threshold = 0.8, winsize = 1024, hopsize = 512, fftsize = 2048;

		server = server ? Server.default;
		if(src.isNil) {Error("Invalid Source Buffer").format(thisMethod.name, this.class.name).throw};
		if(transbuf.isNil) {Error("Invalid Slices Buffer").format(thisMethod.name, this.class.name).throw};
			server.sendMsg(\cmd, \BufNoveltySlice, src, offsetframes, numframes, offsetchans, numchans,
transbuf, kernelsize, threshold, winsize, hopsize, fftsize);



}
}