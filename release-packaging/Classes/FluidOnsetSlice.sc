FluidOnsetSlice : FluidRTUGen {

	const <power = 0;
	const <hfc = 1;
	const <flux = 2;
	const <mkl = 3;
	const <is = 4;
	const <cosine = 5;
	const <phase = 6;
	const <wphase = 7;
	const <complex = 8;
	const <rcomplex = 9;

	*ar { arg in = 0, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;
		^this.multiNew('audio', in.asAudioRateInput(this), metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
