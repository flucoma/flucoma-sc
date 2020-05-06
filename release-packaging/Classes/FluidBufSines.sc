FluidBufSines : UGen{

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackingMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackingMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackingMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1|
		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackingMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize, trig);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackingMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, action|
				^FluidNRTProcess.new(
			server, this, action, [sines, residual].select{|x| x!= -1}
		).process(
			source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackingMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines = -1, residual = -1, bandwidth = 76, detectionThreshold = -96, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 15, trackingMethod = 0, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, windowSize = 1024, hopSize = -1, fftSize = -1, action|
				^FluidNRTProcess.new(
			server, this, action, [sines, residual].select{|x| x!= -1}, blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, detectionThreshold,birthLowThreshold, birthHighThreshold, minTrackLen, trackingMethod, trackMagRange, trackFreqRange, trackProb, windowSize, hopSize, fftSize
		);
	}

}
