FluidOnsetCurve : FluidRTUGen {
	*kr { arg in = 0, metric = 0, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in.asAudioRateInput(this), metric, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if(inputs.at(7).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
