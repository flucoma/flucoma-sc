FluidBufAudioTransport : FluidBufProcessor {

	*kr  { |sourceA, startFrameA = 0, numFramesA = -1, startChanA = 0, numChansA = -1, sourceB, startFrameB = 0, numFramesB = -1, startChanB = 0, numChansB = -1, destination, interpolation = 0.0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		sourceA.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
		sourceB.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
		sourceA = sourceA.asUGenInput;
		sourceB = sourceB.asUGenInput;

		destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
		destination = destination.asUGenInput;


		^FluidProxyUgen.kr(this.objectClassName++\Trigger,-1, sourceA, startFrameA, numFramesA, startChanA, numChansA, sourceB, startFrameA, numFramesA, startChanB, numChansB, destination, interpolation, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}


	*process { |server, sourceA, startFrameA = 0, numFramesA = -1, startChanA = 0, numChansA = -1, sourceB, startFrameB = 0, numFramesB = -1, startChanB = 0, numChansB = -1, destination, interpolation=0.0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		sourceA.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
		sourceB.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
		sourceA = sourceA.asUGenInput;
		sourceB = sourceB.asUGenInput;

		destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
		destination = destination.asUGenInput;

		^this.new(
			server, nil, [destination]
		).processList(
			[sourceA, startFrameA, numFramesA, startChanA, numChansA, sourceB, startFrameB, numFramesB, startChanB, numChansB, destination, interpolation, windowSize, hopSize, fftSize,maxFFTSize,0], freeWhenDone, action
		)
	}

	*processBlocking { |server, sourceA, startFrameA = 0, numFramesA = -1, startChanA = 0, numChansA = -1, sourceB, startFrameB = 0, numFramesB = -1, startChanB = 0, numChansB = -1, destination, interpolation=0.0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		sourceA.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
		sourceB.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
		sourceA = sourceA.asUGenInput;
		sourceB = sourceB.asUGenInput;

		destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
		destination = destination.asUGenInput;

		^this.new(
			server, nil, [destination]
		).processList(
			[sourceA, startFrameA, numFramesA, startChanA, numChansA, sourceB, startFrameB, numFramesB, startChanB, numChansB, destination, interpolation, windowSize, hopSize, fftSize,maxFFTSize,1], freeWhenDone, action
		)
	}
}
FluidBufAudioTransportTrigger : FluidProxyUgen {}
