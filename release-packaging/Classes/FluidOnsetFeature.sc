FluidOnsetFeature : FluidRTUGen {
	*kr { arg in = 0, metric = 0, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		metric = FluidOnsetSlice.prSelectMetric(metric) ?? {
			("% is not a recognised metric").format(metric);
		};

		^this.multiNew('control', in.asAudioRateInput(this), metric, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if([\scalar, \control].includes(inputs.at(1).rate).not) {
			^(": invalid metric");
		};
		if(inputs.at(7).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
