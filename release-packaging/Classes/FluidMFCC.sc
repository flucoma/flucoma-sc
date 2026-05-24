FluidMFCC : FluidRTMultiOutUGen {

	*kr { arg in = 0, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxNumCoeffs = nil, maxNumBands = nil;

		maxNumCoeffs = maxNumCoeffs ? numCoeffs;
		maxNumBands =  maxNumBands ? numBands;

		^this.multiNew('control', in.asAudioRateInput(this), numCoeffs, maxNumCoeffs, numBands, maxNumBands, startCoeff, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize);
	}


	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(4),rate);//this instantiate the number of output from the maxNumBands in the multiNew order
	}

	checkInputs {
		if(inputs.at(2).rate != 'scalar') {
			^(": maxNumCoeffs cannot be modulated.");
		};
		if(inputs.at(4).rate != 'scalar') {
			^(": maxNumBands cannot be modulated.");
		};
		if(inputs.last.rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
