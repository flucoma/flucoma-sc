FluidBufNNDSVD : FluidBufProcessor{

	*kr  { |source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0| 
        
        source.isNil.if {"FluidBufNNDSVD:  Invalid source buffer".throw};
        bases.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        activations.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        source = source.asUGenInput;
        bases = bases.asUGenInput;
        activations = activations.asUGenInput;
        
		^FluidProxyUgen.kr1(\FluidBufNNDSVDTrigger, -1, source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize, trig, blocking);
	}


	*process { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|
        
        source.isNil.if {"FluidBufNNDSVD:  Invalid source buffer".throw};
        bases.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        activations.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        source = source.asUGenInput;
        bases = bases.asUGenInput;
        activations = activations.asUGenInput;
                
		^this.new(
			server, nil, [bases]
		).processList(
			[source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize,0],freeWhenDone, action
		)
	}

    *processBlocking { |server, source, bases, activations, minComponents = 1, maxComponents = 200, coverage = 0.5, method = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|
        
        source.isNil.if {"FluidBufNNDSVD:  Invalid source buffer".throw};
        bases.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        activations.isNil.if {"FluidBufNNDSVD:  Invalid bases buffer".throw};
        source = source.asUGenInput;
        bases = bases.asUGenInput;
        activations = activations.asUGenInput;
                
		^this.new(
			server, nil, [bases]
		).processList(
			[source, bases, activations, minComponents, maxComponents, coverage, method, windowSize, hopSize, fftSize,1],freeWhenDone, action
		)
	}
}
FluidBufNNDSVDTrigger : FluidProxyUgen {}
