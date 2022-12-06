FluidPitch : FluidRTMultiOutUGen {

	const <features=#[\pitch, \confidence];
	classvar <featuresLookup;

	*initClass {
		featuresLookup = FluidProcessSelect(this, this.features);
	}

	*kr { arg in = 0, select, algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		var selectbits = this.featuresLookup.encode(select);

		^this.multiNew('control', in.asAudioRateInput(this), selectbits, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		var numChannels;
		inputs = theInputs;
		numChannels = inputs.at(1).asBinaryDigits.sum;
		^this.initOutputs(numChannels,rate);
	}

	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
