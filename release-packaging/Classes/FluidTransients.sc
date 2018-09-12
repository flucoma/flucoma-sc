FluidTransients : MultiOutUGen {
	*ar { arg in = 0, order = 20, blocksize = 256, padding = 128, skew = 0.0, threshFwd = 3.0, threshBack = 1.1, winSize=14, debounce=25;
		^this.multiNew('audio', in.asAudioRateInput(this), order, blocksize, padding, skew,threshFwd ,threshBack, winSize)
	}
	init { arg ... theInputs;
		inputs = theInputs;
		channels = [
			OutputProxy(rate, this, 0),
			OutputProxy(rate, this, 1)
		];
		^channels
	}
	checkInputs { ^this.checkNInputs(1) }
}