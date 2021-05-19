FluidSpectralShape : FluidRTMultiOutUGen {

	*kr { arg in = 0, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(7,rate);
	}

	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
