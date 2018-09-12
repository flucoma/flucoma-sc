FluidHPSS : MultiOutUGen {
	*ar { arg in = 0, percussiveFilterSize = 17, harmonicFilterSize = 17, percussiveThreshold = 50, harmonicThreshold = 50,  windowSize= 1024, hopSize= 256, fftSize= -1;
		^this.multiNew('audio', in.asAudioRateInput(this),harmonicFilterSize,percussiveFilterSize,percussiveThreshold,harmonicThreshold,windowSize, hopSize, fftSize)
	}
	init { arg ... theInputs;
		inputs = theInputs;
		channels = [
			OutputProxy(rate, this, 0),
			OutputProxy(rate, this, 1)
		];
		^channels
	}
	checkInputs { ^this.checkNInputs(1) }
}