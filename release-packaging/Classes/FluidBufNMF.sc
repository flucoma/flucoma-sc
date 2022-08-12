FluidBufNMF : FluidBufProcessor
{
	*kr  {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth, resynthMode = 0,  bases, basesMode = 0, activations, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};
		resynth = resynth ? -1;
		bases = bases ? -1;
		activations = activations ? -1;

		^FluidProxyUgen.kr(\FluidBufNMFTrigger,-1,source.asUGenInput, startFrame, numFrames, startChan, numChans, resynth.asUGenInput, resynthMode, bases.asUGenInput, basesMode, activations.asUGenInput, actMode, components, iterations, windowSize, hopSize, fftSize, fftSize, trig, blocking);
	}

	*process { |server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth = -1, resynthMode = 0,  bases = -1, basesMode = 0, activations = -1, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|

		source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};
		resynth = resynth ? -1;
		bases = bases ? -1;
		activations = activations ? -1;

		^this.new(
			server,nil,[resynth, bases, activations].select{|x| x!= -1}
		).processList([source, startFrame, numFrames, startChan, numChans, resynth, resynthMode, bases, basesMode, activations, actMode, components,iterations, windowSize, hopSize, fftSize, fftSize, 0],freeWhenDone,action);
	}

	*processBlocking { |server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth = -1, resynthMode = 0, bases = -1, basesMode = 0, activations = -1, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|

		source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};
		resynth = resynth ? -1;
		bases = bases ? -1;
		activations = activations ? -1;

		^this.new(
			server,nil,[resynth, bases, activations].select{|x| x!= -1}
		).processList([source, startFrame, numFrames, startChan, numChans, resynth, resynthMode, bases, basesMode, activations, actMode, components,iterations, windowSize, hopSize, fftSize, fftSize, 1],freeWhenDone,action);
	}
}
FluidBufNMFTrigger : FluidProxyUgen {}
