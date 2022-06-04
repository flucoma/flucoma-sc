FluidNoveltyFeature : FluidRTUGen {
	*kr { arg in = 0, algorithm = 0, kernelSize = 3, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxKernelSize, maxFilterSize;
        
        maxKernelSize = maxKernelSize ? kernelSize; 
        maxFilterSize = maxFilterSize ? filterSize; 
        
		^this.multiNew('control', in.asAudioRateInput(this), algorithm, kernelSize, maxKernelSize, filterSize, maxFilterSize, windowSize, hopSize, fftSize, maxFFTSize)
	}
    
	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		if(inputs.at(3).rate != 'scalar') {
			^(": maxKernelSize cannot be modulated.");
			};
		if(inputs.at(5).rate != 'scalar') {
			^(": maxFilterSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
