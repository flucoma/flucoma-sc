FluidSines : FluidRTMultiOutUGen {
	*ar { arg in = 0, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = -1;
		^this.multiNew('audio', in.asAudioRateInput(this), bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize, maxFFTSize)
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
		if(inputs.at(13).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkNInputs(1)
	}
}
