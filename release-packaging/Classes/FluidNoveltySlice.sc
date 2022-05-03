FluidNoveltySlice : FluidRTUGen {

	const <spectrum = 0;
	const <mfcc = 1;
	const <chroma = 2;
	const <pitch = 3;
	const <loudness = 4;

	*ar { arg in = 0, algorithm = 0, kernelSize = 3, threshold = 0.8, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = 16384, maxKernelSize = 101, maxFilterSize = 100;
		^this.multiNew('audio', in.asAudioRateInput(this), algorithm, kernelSize, threshold, filterSize, minSliceLength, windowSize, hopSize, fftSize, maxFFTSize, maxKernelSize, maxFilterSize)
	}

	checkInputs {
		if(inputs.at(8).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		if(inputs.at(9).rate != 'scalar') {
			^(": maxKernelSize cannot be modulated.");
			};
		if(inputs.at(10).rate != 'scalar') {
			^(": maxFilterSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
