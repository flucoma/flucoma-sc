FluidNoveltySlice : FluidRTUGen {

	const <spectrum = 0;
	const <mfcc = 1;
	const <chroma = 2;
	const <pitch = 3;
	const <loudness = 4;

	*ar { arg in = 0, algorithm = 0, kernelSize = 3, threshold = 0.8, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxKernelSize, maxFilterSize;
        
        maxKernelSize = maxKernelSize ? kernelSize;
        maxFilterSize = maxFilterSize ? filterSize; 
        
		^this.multiNew('audio', in.asAudioRateInput(this), algorithm, kernelSize, maxKernelSize, threshold, filterSize,  maxFilterSize, minSliceLength, windowSize, hopSize, fftSize, maxFFTSize)
	}

	checkInputs {
		if(inputs.at(11).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		if(inputs.at(3).rate != 'scalar') {
			^(": maxKernelSize cannot be modulated.");
			};
		if(inputs.at(6).rate != 'scalar') {
			^(": maxFilterSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
