FluidBufExperiments {

	*allocMatch{|server, srcbuf, rank=1|
		var dstbuf,srcbufnum;

		srcbufnum = srcbuf.bufnum;

		server = server ? Server.default;
		dstbuf = Buffer.new(server:server,numFrames:0,numChannels:1);

		server.listSendMsg(
			[\b_gen, srcbufnum, "BufMatch",dstbuf.bufnum, rank]
			);
		^dstbuf;
	}


	*allocMatchAsync{|server, srcbuf, rank=1|

	var dstbuf,srcbufnum;

		srcbufnum = srcbuf.bufnum;
		server = server ? Server.default;
		dstbuf = Buffer.new(server:server,numFrames:0,numChannels:1);
		server.sendMsg(\cmd, \AsyncBufMatch, srcbufnum, dstbuf.bufnum, rank);
		^dstbuf;
	}

}
