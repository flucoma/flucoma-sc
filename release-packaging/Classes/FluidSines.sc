FluidSines : MultiOutUGen {
	*ar { arg in = 0, bandwidth = 76, thresh = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1.0, winSize= 2048, hopSize= 512, fftSize= 8192;
		^this.multiNew('audio', in.asAudioRateInput(this), bandwidth, thresh, minTrackLen, magWeight,freqWeight ,winSize, hopSize, fftSize)
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