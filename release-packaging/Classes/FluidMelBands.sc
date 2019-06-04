FluidMelBands : MultiOutUGen {

	*kr { arg in = 0, numBands = 40, minFreq = 20, maxFreq = 20000,  maxNumBands = 120, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), numBands, minFreq, maxFreq,  maxNumBands, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(4),rate);
	}

	checkInputs {
		if(inputs.at(4).rate != 'scalar') {
			^(": maxNumBands cannot be modulated.");
		};
		if(inputs.at(8).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};^this.checkValidInputs;
	}
}
