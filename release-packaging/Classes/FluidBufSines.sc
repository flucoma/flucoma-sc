FluidBufSines : UGen{

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize, maxFFTSize, doneAction, blocking);
	}

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize, doneAction);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|
				^FluidNRTProcess.new(
			server, this, action, [sines, residual].select{|x| x!= -1}
		).process(
			source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|
				^FluidNRTProcess.new(
			server, this, action, [sines, residual].select{|x| x!= -1}, blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize
		);
	}

}
