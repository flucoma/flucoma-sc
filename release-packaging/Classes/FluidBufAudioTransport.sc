FluidBufAudioTransport : FluidBufProcessor {

    *objectClassName{
        ^\FluidBufAudioTransp
    }

	*kr  { |source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
        source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
        source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
        source1 = source1.asUGenInput;
        source2 = source2.asUGenInput;

        destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
        destination = destination.asUGenInput;


		^FluidProxyUgen.kr(this.objectClassName++\Trigger,-1, source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame1, numFrames1, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}


	*process { |server, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
        source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
        source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
        source1 = source1.asUGenInput;
        source2 = source2.asUGenInput;

        destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
        destination = destination.asUGenInput;

		^this.new(
			server, nil, [destination]
		).processList(
			[source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame2, numFrames2, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize,maxFFTSize,0], freeWhenDone, action
		)
	}

    *processBlocking { |server, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

        var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
        source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
        source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
        source1 = source1.asUGenInput;
        source2 = source2.asUGenInput;

        destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
        destination = destination.asUGenInput;

		^this.new(
			server, nil, [destination]
		).processList(
			[source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame2, numFrames2, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize,maxFFTSize,1], freeWhenDone, action
		)
	}
}
