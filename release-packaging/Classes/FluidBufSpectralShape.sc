FluidBufSpectralShape : FluidBufProcessor {

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		var selectbits = FluidSpectralShape.featuresLookup.encode(select);
		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^FluidProxyUgen.kr(this.objectClassName++\Trigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);

	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		var selectbits = FluidSpectralShape.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits,  minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		var selectbits = FluidSpectralShape.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize, 1], freeWhenDone, action
		);
	}
}
FluidBufSpectralShapeTrigger : FluidProxyUgen {}
