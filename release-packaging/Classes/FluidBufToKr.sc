FluidKrToBuf {
	*kr {
		arg krStream, buffer;
		if(buffer.numFrames == 0) {"FluidKrToBuf: UGen will have 0 outputs!".warn};
		if(buffer.numFrames > 1000) {"FluidKrToBuf: Buffer is % frames. This is probably not the buffer you intended.".format(buffer.numFrames).error};
		^buffer.numFrames.do{
			arg i;
			BufWr.kr(krStream[i], buffer, i);
		}
	}
}

FluidBufToKr {
	*kr {
		arg buffer;
		if(buffer.numFrames > 1000) {"FluidKrToBuf: Buffer is % frames. This is probably not the buffer you intended.".format(buffer.numFrames).error};
		^buffer.numFrames.collect{
			arg i;
			BufRd.kr(1,buffer,i,0,0);
		}
	}
}