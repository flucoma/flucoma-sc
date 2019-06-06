FluidBufNMF {
	*process { arg server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, bases, basesMode = 0, activations, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1, action;


		source = source.asUGenInput;
		destination = destination.asUGenInput;
		bases = bases.asUGenInput;
		activations = activations.asUGenInput;

		source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};

		server = server ? Server.default;

		destination = destination ? -1;
		bases = bases ? -1;
		activations = activations ? -1;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufNMF, source, startFrame, numFrames, startChan, numChans, destination, bases, basesMode, activations, actMode, components, iterations, windowSize, hopSize, fftSize);
			server.sync;
			if (destination != -1) {destination = server.cachedBufferAt(destination); destination.updateInfo; server.sync;} {destination = nil};
			if (bases != -1) {bases = server.cachedBufferAt(bases); bases.updateInfo; server.sync;} {bases = nil};
			if (activations != -1) {activations = server.cachedBufferAt(activations); activations.updateInfo; server.sync;} {activations = nil};
			action.value(destination, bases, activations);
		};
	}
}
