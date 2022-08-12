FluidOnsetSlice : FluidRTUGen {

	const <metrics = #[
		\power,
		\hfc,
		\flux,
		\mkl,
		\is,
		\cosine,
		\phase,
		\wphase,
		\complex,
		\rcomplex,
	];

	*prSelectMetric { |sym|
		if (sym.isUGen) { ^sym };
		if (sym.isNumber) {
			if (sym >= 0 && (sym < metrics.size)) {
				^sym
			} {
				^nil
			}
		};
		^metrics.indexOf(sym.asSymbol)
	}

	*ar { arg in = 0, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		metric = this.prSelectMetric(metric) ?? {
			("% is not a recognised metric").format(metric);
		};

		^this.multiNew('audio', in.asAudioRateInput(this), metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize)
	}
	checkInputs {
		if([\scalar, \control].includes(inputs.at(1).rate).not) {
			^(": invalid metric");
		};
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
