FluidHPSS : MultiOutUGen {
	*ar { arg in = 0, harmFilterSize=17, percFilterSize = 17, modeFlag=0, thresholdExplanations, winSize= 1024, hopSize= 256, fftSize= -1;
		^this.multiNew('audio', in.asAudioRateInput(this), percFilterSize, harmFilterSize, modeFlag, thresholdExplanations, winSize, hopSize, fftSize)
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