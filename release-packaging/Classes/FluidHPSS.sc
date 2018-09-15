FluidHPSS : MultiOutUGen {
	*ar { arg in = 0, percussiveFilterSize = 17, harmonicFilterSize=17, harmonicBinaryMask=0, percussiveBinaryMask=0,
		percussiveThreshFreq1=0, percussiveThreshAmp1=0,percussiveThreshFreq2=1.0, percussiveThreshAmp2=0,
		harmonicThreshFreq1=0, harmonicThreshAmp1=0, harmonicThreshFreq2=1.0, harmonicThreshAmp2=0,
		windowSize= 1024, hopSize= 256, fftSize= -1;
		^this.multiNew('audio', in.asAudioRateInput(this), percussiveFilterSize, harmonicFilterSize,
			harmonicBinaryMask,percussiveBinaryMask,
			percussiveThreshFreq1, percussiveThreshAmp1,percussiveThreshFreq2, percussiveThreshAmp2,
			harmonicThreshFreq1, harmonicThreshAmp1, harmonicThreshFreq2, harmonicThreshAmp2,
			windowSize, hopSize, fftSize)
	}
	init { arg ... theInputs;
		inputs = theInputs;
		channels = [
			OutputProxy(rate, this, 0),
			OutputProxy(rate, this, 1),
			OutputProxy(rate, this, 2)
		];
		^channels
	}
	checkInputs { ^this.checkNInputs(1) }
}