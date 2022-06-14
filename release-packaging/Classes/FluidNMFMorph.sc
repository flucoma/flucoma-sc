FluidNMFMorph : FluidRTUGen {

	*ar { arg source = -1, target = -1, activations = -1, autoassign = 1, interpolation = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		source = source ?? {-1};
		target = target ?? {-1};
		activations = activations ?? {-1};

		^this.new1('audio', source, target, activations, autoassign, interpolation, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		specialIndex = -1;
	}

	checkInputs {
		if(inputs.last.rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
