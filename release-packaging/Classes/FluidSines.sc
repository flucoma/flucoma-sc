FluidSines : MultiOutUGen {
	*ar { arg in = 0, bw = 76, thresh = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1.0, winSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize=16384;
		^this.multiNew('audio', in.asAudioRateInput(this), bw, thresh, minTrackLen, magWeight, freqWeight ,winSize, hopSize, fftSize, maxFFTSize)
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
