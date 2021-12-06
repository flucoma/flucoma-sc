FluidKrToBuf {
	*kr {
		arg krStream, buffer;

		if(buffer.isKindOf(Buffer).or(buffer.isKindOf(LocalBuf)),{
			if(buffer.numFrames == 0) {"FluidKrToBuf:kr Buffer has 0 frames".warn};
			if(buffer.numFrames > 1000) {
				Error("FluidKrToBuf:kr Buffer is % frames. This is probably not the buffer you intended.".format(buffer.numFrames)).throw;
			};
		});

		^krStream.numChannels.do{
			arg i;
			BufWr.kr(krStream[i], buffer, i);
		}
	}
}

FluidBufToKr {
	*kr {
		arg buffer, numFrames;

		if((buffer.isKindOf(Buffer).or(buffer.isKindOf(LocalBuf))).not.and(numFrames.isNil),{
			Error("FluidBufToKr:kr needs to be passed either an existing buffer or an OutputProxy and a number of frames for the buffer that will be supplied").throw;
		});

		numFrames = numFrames ?? {buffer.numFrames};

		if(numFrames == 0) {"FluidKrToBuf:kr indicated numFrames is zero.".warn};
		if(numFrames > 1000) {
			Error("FluidKrToBuf: Buffer is indicated to have % frames. This is probably not the buffer you intended.".format(numFrames)).throw;
		};

		if(numFrames > 1,{
			^numFrames.collect{
				arg i;
				BufRd.kr(1,buffer,i,0,0);
			}
		},{
			^BufRd.kr(1,buffer,0,0,0);
		});
	}
}
