FluidBufSpectralShape : UGen {

    *kr{ |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufSpectralShape:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufSpectralShape:  Invalid features buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, features, windowSize, hopSize, fftSize, maxFFTSize, doneAction);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [features]
		).process(
			 source, startFrame, numFrames, startChan, numChans, features, windowSize, hopSize, fftSize
		);
	}

}
