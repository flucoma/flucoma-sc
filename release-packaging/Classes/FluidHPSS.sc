FluidHPSS : MultiOutUGen {
	*ar { arg in = 0, harmFiltSize=17, percFiltSize = 17, modeFlag=0, htf1 = 0.1, hta1 = 0, htf2 = 0.5, hta2 = 0, ptf1 = 0.1, pta1 = 0, ptf2 = 0.5, pta2 = 0, winSize= 1024, hopSize= 256, fftSize= -1;
		^this.multiNew('audio', in.asAudioRateInput(this), percFiltSize, harmFiltSize, modeFlag, htf1, hta1, htf2, hta2, ptf1, pta1, ptf2, pta2, winSize, hopSize, fftSize)
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
	checkInputs { ^this.checkNInputs(1) }
}