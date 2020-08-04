FluidBufNNDSVD : UGen{
	*new1 { |rate, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking=0|

		source.isNil.if {"FluidBufNNDSVD:  Invalid source buffer".throw};
		bases.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
		activations.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
  	source = source.asUGenInput;
		bases = bases.asUGenInput;
		activations = activations.asUGenInput;
    
		^super.new1(rate, source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, trig, blocking)

	}

	*kr  { |source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
		^this.new1(\control, source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, trig, blocking);
	}


	*process { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [bases]
		).process(
			source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize
		)
	}

	*processBlocking { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, action|
		^FluidNRTProcess.new(
			server, this, action, [bases],blocking:1
		).process(
			source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize
		)
	}
}
