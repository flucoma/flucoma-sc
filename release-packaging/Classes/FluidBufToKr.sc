FluidKrToBuf {
	*kr {
		arg krStream, buffer;
		^buffer.numFrames.do{
			arg i;
			BufWr.kr(krStream[i], buffer, i);
		}
	}
}

FluidBufToKr {
	*kr {
		arg buffer;
		^buffer.numFrames.collect{
			arg i;
			BufRd.kr(1,buffer,i,0,0);
		}
	}
}