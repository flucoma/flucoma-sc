FluidBufMelBands : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, scale = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^FluidProxyUgen.kr(\FluidBufMelBandsTrigger,-1, source, startFrame, numFrames, startChan, numChans, features, padding, numBands, numBands,  minFreq, maxFreq,  normalize, scale, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, scale = 0,  windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numBands, numBands,  minFreq, maxFreq,  normalize, scale, windowSize, hopSize, fftSize, maxFFTSize, 0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, scale = 0,  windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, numBands, numBands,  minFreq, maxFreq,  normalize, scale, windowSize, hopSize, fftSize, maxFFTSize, 1],freeWhenDone,action
		);
	}
}
FluidBufMelBandsTrigger : FluidProxyUgen {}
