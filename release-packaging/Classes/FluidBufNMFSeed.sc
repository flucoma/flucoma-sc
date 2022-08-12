FluidBufNMFSeed : FluidBufProcessor{

	*kr  { |source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		source.isNil.if {"FluidBufNMFSeed:  Invalid source buffer".throw};
		bases.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		activations.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		source = source.asUGenInput;
		bases = bases.asUGenInput;
		activations = activations.asUGenInput;

		^FluidProxyUgen.kr1(\FluidBufNMFSeedTrigger, -1, source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, fftSize, trig, blocking);
	}


	*process { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		source.isNil.if {"FluidBufNMFSeed:  Invalid source buffer".throw};
		bases.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		activations.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		source = source.asUGenInput;
		bases = bases.asUGenInput;
		activations = activations.asUGenInput;

		^this.new(
			server, nil, [bases,activations]
		).processList(
			[source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, fftSize, 0],freeWhenDone, action
		)
	}

	*processBlocking { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		source.isNil.if {"FluidBufNMFSeed:  Invalid source buffer".throw};
		bases.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		activations.isNil.if {"FluidBufNMFSeed:  Invalid bases buffer".throw};
		source = source.asUGenInput;
		bases = bases.asUGenInput;
		activations = activations.asUGenInput;

		^this.new(
			server, nil, [bases,activations]
		).processList(
			[source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, fftSize, 1],freeWhenDone, action
		)
	}
}
FluidBufNMFSeedTrigger : FluidProxyUgen {}
