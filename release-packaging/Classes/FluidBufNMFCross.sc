FluidBufNMFCross : FluidBufProcessor {

	*kr  { |source, target, output , timeSparsity = 7, polyphony = 10, continuity = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		source = this.validateBuffer(source, "source");
		target = this.validateBuffer(target, "target");
		output = this.validateBuffer(output, "output");

		^FluidProxyUgen.kr(\FluidBufNMFCrossTrigger, -1, source, target, output, timeSparsity, polyphony, continuity, iterations,  windowSize, hopSize, fftSize, fftSize, trig, blocking);
	}

	*process { |server, source, target, output , timeSparsity = 7, polyphony = 10, continuity = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		target = this.validateBuffer(target, "target");
		output = this.validateBuffer(output, "output");

		^this.new(
			server, nil, [output]
		).processList(
			[source, target, output, timeSparsity, polyphony, continuity, iterations,  windowSize, hopSize, fftSize, fftSize, 0],freeWhenDone, action
		);
	}

	*processBlocking { |server, source, target, output , timeSparsity = 7, polyphony = 10, continuity = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		source = this.validateBuffer(source, "source");
		target = this.validateBuffer(target, "target");
		output = this.validateBuffer(output, "output");

		^this.new(
			server, nil, [output]
		).processList(
			[source, target, output, timeSparsity, polyphony, continuity, iterations,  windowSize, hopSize, fftSize, fftSize, 1],freeWhenDone, action
		);
	}
}
FluidBufNMFCrossTrigger : FluidProxyUgen {}
