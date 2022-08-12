FluidBufMFCC : FluidBufProcessor{
	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufMFCC:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufMFCC:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufMFCCTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, numCoeffs, numCoeffs, numBands, numBands, startCoeff, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize,trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone=true, action |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufMFCC:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufMFCC:  Invalid features buffer".throw};

		^this.new(
			server, nil,[features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numCoeffs, numCoeffs, numBands,  numBands, startCoeff, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, numCoeffs = 13, numBands = 40, startCoeff = 0, minFreq = 20, maxFreq = 20000, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone=true, action |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufMFCC:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufMFCC:  Invalid features buffer".throw};

		^this.new(
			server, nil,[features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numCoeffs, numCoeffs, numBands, numBands, startCoeff, minFreq, maxFreq, windowSize, hopSize, fftSize, maxFFTSize,1],freeWhenDone,action
		);
	}
}
FluidBufMFCCTrigger : FluidProxyUgen {}
