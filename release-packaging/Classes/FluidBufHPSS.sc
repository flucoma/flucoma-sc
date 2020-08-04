FluidBufHPSS : UGen {

	*new1 {|rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		harmonic = harmonic.asUGenInput;
		percussive = percussive.asUGenInput;
		residual = residual.asUGenInput;
		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

        ^super.new1(rate, source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize, harmFilterSize, percFilterSize, trig, blocking);
	}

	*kr  {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 

        ^this.multiNew(
            'control', source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, trig, blocking
        );
	}

   *process {|server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, action|

		^FluidNRTProcess.new(
			server, this, action, [harmonic, percussive, residual].select{|x| x!= -1}
		).process(
			source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize
		);

	}

       *processBlocking {|server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, action|

		^FluidNRTProcess.new(
			server, this, action, [harmonic, percussive, residual].select{|x| x!= -1}, blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize
		);

	}
}
