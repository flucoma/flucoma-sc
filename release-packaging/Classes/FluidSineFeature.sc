FluidSineFeature : FluidRTMultiOutUGen {
	*kr { arg in = 0, bandwidth = 76, numPeaks = 10, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackingMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = -1, maxNumPeaks = nil;

		maxNumPeaks = maxNumPeaks ? numPeaks;

		^this.multiNew('control', in.asAudioRateInput(this), bandwidth, numPeaks, maxNumPeaks, detectionThreshold, birthLowThreshold, birthHighThreshold, minTrackLen, trackingMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize, maxFFTSize)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(3),rate);//this instantiate the number of output from the maxNumPeaks in the multiNew order
	}

	checkInputs {
		if(inputs.at(14).rate != 'scalar') {
			^(": maxNumPeaks cannot be modulated.");
		};
		if(inputs.at(13).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
