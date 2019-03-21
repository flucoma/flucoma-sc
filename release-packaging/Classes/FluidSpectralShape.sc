FluidSpectralShape : MultiOutUGen {

	*kr { arg in = 0, winSize = 1024, hopSize = 512, fftSize = -1, maxFFTSize = 16384;
		^this.multiNew('control', in, winSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		^this.initOutputs(7,rate);
	}

	checkInputs {
		if (inputs.at(0).rate != 'audio', {
			^(" input 0 is not audio rate");
		});
		^this.checkValidInputs;
	}
}
