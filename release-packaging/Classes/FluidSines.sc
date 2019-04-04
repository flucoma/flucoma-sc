FluidSines : MultiOutUGen {
	*ar { arg in = 0, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1.0, winSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize=16384;
		^this.multiNew('audio', in.asAudioRateInput(this), bandwidth, threshold, minTrackLen, magWeight, freqWeight ,winSize, hopSize, fftSize, maxFFTSize)
	}
	init { arg ... theInputs;
		inputs = theInputs;
		channels = [
			OutputProxy(rate, this, 0),
			OutputProxy(rate, this, 1)
		];
		^channels
	}
	checkInputs {
		if(inputs.at(9).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
			};
		^this.checkNInputs(1)
	}
}
