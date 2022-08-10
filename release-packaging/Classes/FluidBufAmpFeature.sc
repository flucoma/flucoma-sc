FluidBufAmpFeature : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, trig = 1, blocking = 0|

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufAmpFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufAmpFeature:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufAmpFeatureTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq, trig, blocking);
	}

	*process { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, freeWhenDone = true, action |

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufAmpFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufAmpFeature:  Invalid features buffer".throw};

		^this.new(server, nil, [features]).processList(
			[source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq,0],freeWhenDone, action
		);
	}

	*processBlocking { |server,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85, freeWhenDone = true, action |

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufAmpFeature:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufAmpFeature:  Invalid features buffer".throw};

		^this.new(server, nil, [features]).processList(
			[source, startFrame, numFrames, startChan, numChans, features, fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq,1],freeWhenDone, action
		);
	}
}
FluidBufAmpFeatureTrigger : FluidProxyUgen {}
