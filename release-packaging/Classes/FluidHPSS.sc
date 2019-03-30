FluidHPSS : MultiOutUGen {
	*ar { arg in = 0, hFiltSize=17, pFiltSize = 31, modeFlag=0, htf1 = 0.1, hta1 = 0, htf2 = 0.5, hta2 = 0, ptf1 = 0.1, pta1 = 0, ptf2 = 0.5, pta2 = 0, winSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = 16384, maxHFlitSize = 101, maxPFiltSize = 101;
		^this.multiNew('audio', in.asAudioRateInput(this), hFiltSize, pFiltSize, modeFlag, htf1, hta1, htf2, hta2, ptf1, pta1, ptf2, pta2, winSize, hopSize, fftSize, maxFFTSize, maxHFlitSize, maxPFiltSize)
	}
	init { arg ... theInputs;
		inputs = theInputs;
		channels = [
			OutputProxy(rate, this, 0),
			OutputProxy(rate, this, 1),
			OutputProxy(rate, this, 2)
		];
		^channels
	}
		checkInputs {
		if(inputs.at(15).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		if(inputs.at(16).rate != 'scalar') {
			^(": maxHFlitSize cannot be modulated.");
			};
		if(inputs.at(17).rate != 'scalar') {
			^(": maxPFiltSize cannot be modulated.");
			};
		^this.checkValidInputs;
	}
}
