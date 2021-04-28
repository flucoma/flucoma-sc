FluidOnsetSlice : FluidRTUGen {
	*ar { arg in = 0, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput(this), metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
