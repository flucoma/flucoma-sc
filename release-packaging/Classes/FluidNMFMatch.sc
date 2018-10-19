FluidNMFMatch : MultiOutUGen {

	var <rank;

	*kr { arg in = 0, filterBuf, rank = 1, iterations = 10, winsize = 1024, hopsize = 256, fftsize = -1;
		^this.multiNew('control', in.asAudioRateInput(this), filterBuf, rank, iterations, winsize, hopsize, fftsize);
	}


	init{arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs[2],rate);
	}
/*	checkInputs {
		if (rate == 'audio', {
			if (inputs.at(0).rate != 'audio', {
				^(" input 0 is not audio rate");
				});
		}, {
			if(inputs.size <= this.class.numFixedArgs, {
				^"missing input at index 1"
			})
		});
	}*/

/*	*numFixedArgs { ^1 }
	writesToBus { ^false}*/

}



