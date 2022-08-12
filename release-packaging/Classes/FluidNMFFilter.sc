FluidNMFFilter : FluidRTMultiOutUGen {

	*ar { arg in = 0, bases, maxComponents = 1, iterations = 10, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;
		^this.multiNew('audio', in.asAudioRateInput(this), bases, maxComponents, maxComponents, iterations, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs[2],rate);
	}

	checkInputs {
		if(inputs.at(3).rate != 'scalar') {
			^(": maxComponents cannot be modulated.");
		};
		if(inputs.at(8).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
