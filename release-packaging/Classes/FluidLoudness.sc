FluidLoudness : FluidRTMultiOutUGen {

	const <features=#[\loudness, \peak];
	classvar <featuresLookup;

	*initClass {
		featuresLookup = FluidProcessSelect(this, this.features);
	}

	*kr { arg in = 0, select, kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, maxWindowSize = 16384;

		var selectbits = this.featuresLookup.encode(select);

		^this.multiNew('control', in.asAudioRateInput(this), selectbits, kWeighting, truePeak, windowSize, hopSize, maxWindowSize);
	}

	init {arg ...theInputs;
		var numChannels;
		inputs = theInputs;
		numChannels = inputs.at(1).asBinaryDigits.sum;
		^this.initOutputs(numChannels,rate);
	}

	checkInputs {
		if(inputs.at(6).rate != 'scalar') {
			^(": maxwindowSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
