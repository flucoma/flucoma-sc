FluidAmpFeature : FluidRTUGen {
	*ar { arg in = 0, fastRampUp = 1, fastRampDown = 1, slowRampUp = 100, slowRampDown = 100, floor = -144, highPassFreq = 85;

		^this.multiNew('audio', in.asAudioRateInput(this), fastRampUp, fastRampDown, slowRampUp, slowRampDown, floor, highPassFreq)
	}
	checkInputs {
		^this.checkValidInputs;
	}
}
