FluidBufAudioTransport : UGen{
	*new1 { |rate, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking=0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};
		source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
		source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
		source1 = source1.asUGenInput;
		source2 = source2.asUGenInput;

		destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
		destination = destination.asUGenInput;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)
		^super.new1(rate,source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame1, numFrames1, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize, maxFFTSize, trig,blocking)

	}

	*kr  { |source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
		^this.new1(\control, source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame1, numFrames1, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize, trig, blocking);
	}


	*process { |server, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [destination]
		).process(
			source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame2, numFrames2, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize
		)
	}

	*processBlocking { |server, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [destination],blocking:1
		).process(
			source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame2, numFrames2, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize
		)
	}
}

