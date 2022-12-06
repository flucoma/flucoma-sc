FluidBufAmpFeature : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, trig = 1, blocking = 0|

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^FluidProxyUgen.kr(\FluidBufAmpFeatureTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq, trig, blocking);
	}

	*process { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, freeWhenDone = true, action |

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(server, nil, [features]).processList(
			[source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq,0],freeWhenDone, action
		);
	}

	*processBlocking { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, freeWhenDone = true, action |

		source = this.validateBuffer(source, "source");
		features = this.validateBuffer(features, "features");

		^this.new(server, nil, [features]).processList(
			[source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq,1],freeWhenDone, action
		);
	}
}
FluidBufAmpFeatureTrigger : FluidProxyUgen {}
