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
		arg buffer numFrames, startFrame=0;

		if(buffer.isKindOf(Buffer) or: {buffer.isKindOf(LocalBuf)}, {
			numFrames = numFrames ?? {buffer.numFrames - startFrame};
		}, {
			numFrames = numFrames ? 1;
		});

		if(numFrames > 1000) {
			Error("%: numframes is % frames. This is probably not what you intended.".format(this.class, numFrames)).throw;
		};

		if(numFrames > 1,{
			^numFrames.collect{
				arg i;
				BufRd.kr(1,buffer,i+startFrame,0,0);
			}
		},{
			^BufRd.kr(1,buffer,startFrame,0,0);
		});
	}
}
