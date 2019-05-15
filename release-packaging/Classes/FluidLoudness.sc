FluidLoudness : MultiOutUGen {
	*kr { arg in = 0, kWeighting = 1, truePeak = 1, winSize = 1024, hopSize = 512, maxWinSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), kWeighting, truePeak, winSize, hopSize, maxWinSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(2,rate);
	}

	checkInputs {
		if(inputs.at(5).rate != 'scalar') {
			^(": maxWinSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
