FluidNMFMatch : MultiOutUGen {

	*kr { arg in = 0, dictBufNum, rank = 1, nIter = 10, winSize = 1024, hopSize = 256, fftSize = -1;
		^this.multiNew('control', in, dictBufNum, rank, nIter, winSize, hopSize, fftSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs[2],rate);
	}

	checkInputs {
		if (inputs.at(0).rate != 'audio', {
			^(" input 0 is not audio rate");
		});
		^this.checkValidInputs;
	}
}



