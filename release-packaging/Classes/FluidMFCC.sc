FluidMFCC : FluidRTMultiOutUGen {

	*kr { arg in = 0, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384, maxNumCoeffs = 40;
		^this.multiNew('control', in.asAudioRateInput(this), numCoeffs, numBands, startCoeff, minFreq, maxFreq, maxNumCoeffs, windowSize, hopSize, fftSize, maxFFTSize);
	}


	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(6),rate);//this instantiate the number of output from the maxNumCoeffs in the multiNew order
	}

	checkInputs {
		// the checks of rates here are in the order of the kr method definition
		if(inputs.at(6).rate != 'scalar') {
			^(": maxNumCoeffs cannot be modulated.");
		};
		if(inputs.at(10).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
