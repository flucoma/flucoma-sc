FluidSines : MultiOutUGen {
	*ar { arg in = 0, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magnitudeWeight = 0.1, frequencyWeight = 1.0, windowSize= 2048, hopSize= 512, fftSize= 8192;
		^this.multiNew('audio', in.asAudioRateInput(this), bandwidth, threshold, minTrackLen, magnitudeWeight,frequencyWeight ,windowSize, hopSize, fftSize)
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