FluidOnsetSlice : UGen {
	*ar { arg in = 0, function = 0, threshold = 0.5, debounce = 2, filterSize = 5, frameDelta = 0, winSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), function, threshold, debounce, filterSize, frameDelta, winSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
