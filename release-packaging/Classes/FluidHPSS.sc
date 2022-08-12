FluidHPSS : FluidRTMultiOutUGen {
	*ar { arg in = 0, harmFilterSize=17, percFilterSize = 31, maskingMode=0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = -1, maxHarmFilterSize, maxPercFilterSize;

		maxHarmFilterSize = maxHarmFilterSize ? harmFilterSize;
		maxPercFilterSize = maxPercFilterSize ? percFilterSize;

		^this.multiNew('audio', in.asAudioRateInput(this), harmFilterSize, maxHarmFilterSize, percFilterSize, maxPercFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize)
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
	checkInputs {
		if(inputs.at(17).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		if(inputs.at(2).rate != 'scalar') {
			^(": maxHarmFilterSize cannot be modulated.");
		};
		if(inputs.at(4).rate != 'scalar') {
			^(": maxPercFilterSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
