FluidNMFMatch : MultiOutUGen {

	*kr { arg in = 0, dictBufNum, maxRank = 1, nIter = 10, winSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), dictBufNum, maxRank, nIter, winSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs[2],rate);
	}

	checkInputs {
		if(inputs.at(7).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
