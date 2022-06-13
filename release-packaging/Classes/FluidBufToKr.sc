FluidKrToBuf {
	*kr {
		arg krStream, buffer, krStartChan = 0, krNumChans = -1, destStartFrame = 0;
		var endChan;

		// fix -1 default
		if(krNumChans == -1,{krNumChans = krStream.numChannels - krStartChan});

		// what is the last channel that will be used
		endChan = (krStartChan + krNumChans) - 1;

		if(buffer.isKindOf(Buffer).or(buffer.isKindOf(LocalBuf)),{

			// sanity check
			if(buffer.numFrames == 0){"% Buffer has 0 frames".format(this.class).warn};

			// oopsie check
			if(buffer.numFrames > 1000){
				Error("% Buffer is % frames. This is probably not the buffer you intended.".format(this.class,buffer.numFrames)).throw;
			};

			// out of bounds check
			if((destStartFrame + krNumChans) > buffer.numFrames,{
				Error("% (destStartFrame + krNumChans) > buffer.numFrames".format(this.class)).throw;
			});

		});

		^(krStartChan..endChan).do{
			arg kr_i, i;
			BufWr.kr(krStream[kr_i], buffer, destStartFrame + i);
		}
	}
}

FluidBufToKr {
	*kr {
		arg buffer, startFrame = 0, numFrames = -1;

		if(buffer.isKindOf(Buffer) or: {buffer.isKindOf(LocalBuf)},{

			// fix default -1
			if(numFrames == -1,{numFrames = buffer.numFrames - startFrame});

			// dummy check
			if(numFrames < 1,{Error("% numFrames must be >= 1".format(this.class)).throw});

			// out of bounds check
			if((startFrame+numFrames) > buffer.numFrames,{Error("% (startFrame + numFrames) > buffer.numFrames".format(this.class)).throw;});

		},{
			// make sure the numFrames give is a positive integer
			if((numFrames < 1) || (numFrames.isInteger.not),{
				Error("% if no buffer is specified, numFrames must be a value >= 1.".format(this.class)).throw;
			});
		});

		// oopsie check
		if(numFrames > 1000) {
			Error("%: numframes is % frames. This is probably not what you intended.".format(this.class, numFrames)).throw;
		};

		if(numFrames > 1,{
			^numFrames.asInteger.collect{
				arg i;
				BufRd.kr(1,buffer,i+startFrame,0,0);
			}
		},{
			^BufRd.kr(1,buffer,startFrame,0,0);
		});
	}
}
