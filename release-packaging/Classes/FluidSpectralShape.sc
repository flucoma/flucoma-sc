FluidSpectralShape : MultiOutUGen {

	*kr { arg in = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(7,rate);
	}

	checkInputs {
		if(inputs.at(4).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
