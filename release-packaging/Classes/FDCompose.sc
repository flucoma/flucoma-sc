FDCompose{
		*process { arg server, src, offsetframes1 = 0, numframes1 = -1, offsetchans1 = 0, numchans1 = -1, src1gain = 1, src1dstoffset = 0, src1dstchanoffset = 0, src2, offsetframes2 = 0, numframes2 = -1, offsetchans2 = 0, numchans2 = -1, src2gain = 1, src2dstoffset = 0, src2dstchanoffset = 0, dstbuf;

		server = server ? Server.default;
		if(src.bufNum.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server.sendMsg(\cmd, \BufCompose, src.buNum, offsetframes1, numframes1, offsetchans1, numchans1, src1gain, src1dstoffset, src1dstchanoffset,
			if( src2.isNil, -1, {src2.bufNum}), offsetframes2, numframes2, offsetchans2, numchans2, src2gain, src2dstoffset, src2dstchanoffset,
			if( dstbuf.isNil, -1, {dstbuf.bufNum}));
	}
}
