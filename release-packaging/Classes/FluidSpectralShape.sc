FluidSpectralShape : FluidRTMultiOutUGen {

	const <features=#[\centroid,\spread,\skewness,\kurtosis,\rolloff,\flatness,\crest];
	classvar <featuresLookup;

	*initClass {
		featuresLookup = FluidProcessSelect(this, this.features);
	}

	*kr { arg in = 0, select, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		var selectbits = this.featuresLookup.encode(select);

		^this.multiNew('control', in.asAudioRateInput(this), selectbits, minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		var numChannels;
		inputs = theInputs;
		numChannels = inputs.at(1).asBinaryDigits.sum;
		^this.initOutputs(numChannels,rate);
	}

	checkInputs {
		if(inputs.at(10).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
