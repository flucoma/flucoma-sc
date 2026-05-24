FluidChroma : FluidRTMultiOutUGen {

	*kr { arg in = 0, numChroma = 12, ref = 440, normalize = 0, minFreq = 0, maxFreq = -1, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxNumChroma;

		maxNumChroma = maxNumChroma ? numChroma;

		^this.multiNew('control', in.asAudioRateInput(this), numChroma, maxNumChroma, ref, normalize, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(2),rate); //this instantiate the number of output from the maxNumChroma in the multiNew order
	}

	checkInputs {
		if(inputs.at(2).rate != 'scalar') {
			^(": maxNumChroma cannot be modulated.");
		};
		if(inputs.last.rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
