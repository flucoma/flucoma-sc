FluidShannonEntropy : FluidRTUGen {
	*ar { arg in = 0, symbolCount=32, winSize=512, maxWinSize=512, kHopSize=256;
		^this.multiNew('audio', in.asAudioRateInput(this), symbolCount, winSize, maxWinSize, kHopSize)
	}
	checkInputs {
		// the checks of rates here are in the order of the kr method definition
		if(inputs.at(2).rate != 'scalar') {
			^(": maxNumCoeffs cannot be modulated.");
		};
		^this.checkValidInputs;
	}

}

