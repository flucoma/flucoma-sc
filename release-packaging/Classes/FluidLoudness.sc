FluidLoudness : MultiOutUGen {
	*kr { arg in = 0, kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, maxwindowSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), kWeighting, truePeak, windowSize, hopSize, maxwindowSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(2,rate);
	}

	checkInputs {
		if(inputs.at(5).rate != 'scalar') {
			^(": maxwindowSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
