FluidShannonEntropyKR : FluidRTUGen {
	*kr { arg in = 0, symbolCount=1000, winSize=100, maxWinSize=200, kHopSize=50;
		^this.multiNew('control', in, symbolCount, winSize, maxWinSize, kHopSize)
	}
	checkInputs {
		// the checks of rates here are in the order of the kr method definition
		if(inputs.at(3).rate != 'scalar') {
			^(": maxWinSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}

}



