FluidNoveltySlice : FluidRTUGen {

	const <algorithms = #[\spectrum, \mfcc, \chroma, \pitch, \loudness];

	*prSelectAlgorithm { |sym|
		if (sym.isUGen) { ^sym };
		if (sym.isNumber) {
			if (sym >= 0 && (sym < algorithms.size)) {
				^sym
			} {
				^nil
			}
		};
		^algorithms.indexOf(sym.asSymbol)
	}

	*ar { arg in = 0, algorithm = 0, kernelSize = 3, threshold = 0.8, filterSize = 1, minSliceLength = 2, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1, maxKernelSize, maxFilterSize;

		maxKernelSize = maxKernelSize ? kernelSize;
		maxFilterSize = maxFilterSize ? filterSize;

		algorithm = this.prSelectAlgorithm(algorithm)  ?? {
			("FluidNoveltySlice: % is not a recognised algorithm").format(algorithm);
		};

		^this.multiNew('audio', in.asAudioRateInput(this), algorithm, kernelSize, maxKernelSize, threshold, filterSize,  maxFilterSize, minSliceLength, windowSize, hopSize, fftSize, maxFFTSize)
	}

	checkInputs {
		if([\scalar, \control].includes(inputs.at(1).rate).not) {
			^(": invalid algorithm");
		};
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
