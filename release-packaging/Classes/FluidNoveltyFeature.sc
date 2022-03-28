FluidNoveltyCurve : FluidRTUGen {
	*kr { arg in = 0, feature = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384, maxKernelSize = 101, maxFilterSize = 100;
		^this.multiNew('control', in.asAudioRateInput(this), feature, kernelSize, filterSize, windowSize, hopSize, fftSize, maxFFTSize, maxKernelSize, maxFilterSize)
	}
	checkInputs {
		if(inputs.at(6).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		if(inputs.at(7).rate != 'scalar') {
			^(": maxKernelSize cannot be modulated.");
			};
		if(inputs.at(8).rate != 'scalar') {
			^(": maxFilterSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
