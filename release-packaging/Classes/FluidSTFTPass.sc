FluidSTFTPass : FluidRTUGen {
	*ar { arg in = 0, windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize;
		^this.multiNew('audio', in.asAudioRateInput(this), windowSize, hopSize = -1, fftSize = -1, maxFFTSize = -1)
	}
	checkInputs {
		if(inputs.at(4).rate != 'scalar') {
			^": maxFFTSize cannot be modulated.";
		};
		^this.checkValidInputs
	}
}
