FluidMFCC : FluidRTMultiOutUGen {

	*kr { arg in = 0, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384, maxNumCoeffs = 40;
		^this.multiNew('control', in.asAudioRateInput(this), numCoeffs, numBands, startCoeff, minFreq, maxFreq,  maxNumCoeffs, windowSize, hopSize, fftSize, maxFFTSize);
	}


	init {arg ...theInputs;
		inputs = theInputs;
		// inputs.at(5).rate.postln;
		^this.initOutputs(inputs.at(5),rate);//this instantiate the number of output from the maxNumCoeffs in the multiNew order
	}

	checkInputs {
		// inputs.at(9).rate.postln;
		// the checks of rates here are in the order of the kr method definition
		if(inputs.at(9).rate != 'scalar') {
			^(": maxNumCoeffs cannot be modulated.");
		};
		if(inputs.at(8).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};^this.checkValidInputs;
	}
}
