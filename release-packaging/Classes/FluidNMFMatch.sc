FluidNMFMatch : FluidRTMultiOutUGen {

	*kr { arg in = 0, bases, maxComponents = 1, iterations = 10, seed = -1, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;
		^this.multiNew('control', in.asAudioRateInput(this), bases, maxComponents, maxComponents, iterations, seed, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs[2],rate);
	}

	checkInputs {
		if(inputs.at(2).rate != 'scalar') {
			^(": maxComponents cannot be modulated.");
		};
		if(inputs.last.rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
