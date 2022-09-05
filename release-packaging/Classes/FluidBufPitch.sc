FluidBufPitch : FluidBufProcessor{

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  select, algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		var selectbits = FluidPitch.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^FluidProxyUgen.kr(\FluidBufPitchTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);

	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		var selectbits = FluidPitch.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  select, algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		var selectbits = FluidPitch.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, 1], freeWhenDone, action
		);
	}
}
FluidBufPitchTrigger : FluidProxyUgen {}
