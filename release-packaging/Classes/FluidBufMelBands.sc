FluidBufMelBands : UGen {
		*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufMelBands:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufMelBands:  Invalid features buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)
		// same for maxNumBands which is passed numBands

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, features, numBands, minFreq, maxFreq, numBands, normalize, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, features, numBands, minFreq, maxFreq, numBands, normalize, windowSize, hopSize, fftSize, trig, blocking);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [features]
		).process(
			source, startFrame, numFrames, startChan, numChans, features, numBands, minFreq, maxFreq, normalize, windowSize, hopSize, fftSize
        );
	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  numBands = 40, minFreq = 20, maxFreq = 20000, normalize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [features], blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, features, numBands, minFreq, maxFreq, normalize, windowSize, hopSize, fftSize
        );
}
}