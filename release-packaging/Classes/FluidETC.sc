FluidETC : UGen {
	*ar { arg in = 0, symbolCount=16, winSize=5, maxWinSize=20, kHopSize=5;
		^this.multiNew('audio', in.asAudioRateInput(this), symbolCount, winSize, maxWinSize, kHopSize)
	}
	checkInputs {
		// the checks of rates here are in the order of the kr method definition
		if(inputs.at(3).rate != 'scalar') {
			^(": maxWinSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}

