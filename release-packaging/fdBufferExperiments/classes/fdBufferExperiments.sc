// adds an instance method to the Buffer class
FDBufferExperiments {

	*allocMatch{|server, srcbuf, rank=1|
		var dstbuf,srcbufnum;
		"Rank" + rank.postln;
		srcbufnum = srcbuf.bufnum;

		server = server ? Server.default;
		dstbuf = Buffer.new(server:server,numFrames:0,numChannels:1);

		server.listSendMsg(
			[\b_gen, srcbufnum, "BufMatch",dstbuf.bufnum, rank]
			);
		^dstbuf;
	}
}
