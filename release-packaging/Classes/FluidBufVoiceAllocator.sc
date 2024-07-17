FluidBufVoiceAllocator : FluidBufProcessor {
	*process { |server, sourceA, startFrameA = 0, numFramesA = -1, startChanA = 0, numChansA = -1, sourceB, startFrameB = 0, numFramesB = -1, startChanB = 0, numChansB = -1, destA, destB, destC, numVoices = 1, prioritisedVoices = 0, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 1, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, freeWhenDone = true, action|

		sourceA.isNil.if {"FluidBufVoiceAllocator:  Invalid source 1 buffer".throw};
		sourceB.isNil.if {"FluidBufVoiceAllocator:  Invalid source 2 buffer".throw};
		sourceA = sourceA.asUGenInput;
		sourceB = sourceB.asUGenInput;

		destA.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destB.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destC.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destA = destA.asUGenInput;
		destB = destB.asUGenInput;
		destC = destC.asUGenInput;

		^this.new(
			server, nil, [destA, destB, destC]
		).processList(
			[sourceA, startFrameA, numFramesA, startChanA, numChansA, sourceB, startFrameB, numFramesB, startChanB, numChansB, destA, destB, destC, numVoices, numVoices, prioritisedVoices, birthLowThreshold, birthHighThreshold, minTrackLen, trackMagRange, trackFreqRange, trackProb, 0], freeWhenDone, action
		)
	}

	*processBlocking { |server, sourceA, startFrameA = 0, numFramesA = -1, startChanA = 0, numChansA = -1, sourceB, startFrameB = 0, numFramesB = -1, startChanB = 0, numChansB = -1, destA, destB, destC, numVoices = 1, prioritisedVoices = 0, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 1, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, freeWhenDone = true, action|

		sourceA.isNil.if {"FluidBufVoiceAllocator:  Invalid source 1 buffer".throw};
		sourceB.isNil.if {"FluidBufVoiceAllocator:  Invalid source 2 buffer".throw};
		sourceA = sourceA.asUGenInput;
		sourceB = sourceB.asUGenInput;

		destA.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destB.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destC.isNil.if {"FluidBufVoiceAllocator:  Invalid destination buffer".throw};
		destA = destA.asUGenInput;
		destB = destB.asUGenInput;
		destC = destC.asUGenInput;

		^this.new(
			server, nil, [destA, destB, destC]
		).processList(
			[sourceA, startFrameA, numFramesA, startChanA, numChansA, sourceB, startFrameB, numFramesB, startChanB, numChansB, destA, destB, destC, numVoices, numVoices, prioritisedVoices, birthLowThreshold, birthHighThreshold, minTrackLen, trackMagRange, trackFreqRange, trackProb, 1], freeWhenDone, action
		)
	}
}

FluidBufVoiceAllocatorTrigger : FluidProxyUgen {}
