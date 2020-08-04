FluidBufNMFCross : UGen{

    *new1 { |rate, source, target, output , timeSparsity = 10, polyphony = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

        source = source.asUGenInput;
		target = target.asUGenInput;
		output = output.asUGenInput;
		source.isNil.if {"FluidBufNMFCross:  Invalid source buffer".throw};
		target.isNil.if {"FluidBufNMFCross:  Invalid target buffer".throw};
        output.isNil.if {"FluidBufNMFCross:  Invalid output buffer".throw};

		^super.new1(rate, source, target, output, timeSparsity, polyphony, iterations,  windowSize, hopSize, fftSize, trig, blocking);
	}

    *kr  { |source, target, output , timeSparsity = 10, polyphony = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
		^this.multiNew(\control, source, target, output, timeSparsity, polyphony, iterations,  windowSize, hopSize, fftSize, trig, blocking);
	}

    *process { |server, source, target, output , timeSparsity = 10, polyphony = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, action|
        ^FluidNRTProcess.new(
			server, this, action, [output].select{|x| x!= -1}
		).process(
			source, target, output, timeSparsity, polyphony, iterations,  windowSize, hopSize, fftSize
		);
	}

    *processBlocking { |server, source, target, output , timeSparsity = 10, polyphony = 7, iterations = 50, windowSize = 1024, hopSize = -1, fftSize = -1, action|
        ^FluidNRTProcess.new(
			server, this, action, [output].select{|x| x!= -1}, blocking: 1
		).process(
			source, target, output, timeSparsity, polyphony, iterations,  windowSize, hopSize, fftSize
		);
	}
}
