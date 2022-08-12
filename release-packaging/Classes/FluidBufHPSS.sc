FluidBufHPSS : FluidBufProcessor {

	*kr  {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;
		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};

		^FluidProxyUgen.kr(\FluidBufHPSSTrigger, -1, source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, harmFilterSize, percFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize,  trig, blocking
		);
	}

	*process {|server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone=true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;
		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};


		^this.new(
			server, nil, [harmonic, percussive, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, harmFilterSize, percFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize, 0], freeWhenDone,action
		);

	}

	*processBlocking {|server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic = -1, percussive = -1, residual = -1, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone=true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;
		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};


		^this.new(
			server, nil, [harmonic, percussive, residual].select{|x| x!= -1}
		).processList(
			[source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, harmFilterSize, percFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize, 1], freeWhenDone,action
		);

	}
}
FluidBufHPSSTrigger : FluidProxyUgen {}
