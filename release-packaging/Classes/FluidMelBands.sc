FluidMelBands : FluidRTMultiOutUGen {

	*kr { arg in = 0, numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, scale = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxNumBands;

		maxNumBands = maxNumBands ? numBands;

		^this.multiNew('control', in.asAudioRateInput(this), numBands, maxNumBands, minFreq, maxFreq, normalize, scale, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(2),rate); //this instantiate the number of output from the maxNumBands in the multiNew order
	}

	checkInputs {
		if(inputs.at(2).rate != 'scalar') {
			^(": maxNumBands cannot be modulated.");
		};
		if(inputs.last.rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};^this.checkValidInputs;
	}
}
