FluidBufLoudness : FluidBufProcessor{

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, trig = 1, blocking = 0|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits = FluidLoudness.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^FluidProxyUgen.kr(\FluidBufLoudnessTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, freeWhenDone = true, action|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits = FluidLoudness.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, freeWhenDone = true, action|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits = FluidLoudness.featuresLookup.encode(select);

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features,padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,1],freeWhenDone,action
		);
	}
}
FluidBufLoudnessTrigger : FluidProxyUgen {}
