FluidBufNMF : FluidBufProcessor //: UGen {
{
    *kr  {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth, bases, basesMode = 0, activations, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1, trig = 1, blocking = 0| 

        source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};        
        resynth = resynth ? -1;
        bases = bases ? -1;
        activations = activations ? -1;        
        
        ^FluidProxyUgen.kr(\FluidBufNMFTrigger,-1,source, startFrame, numFrames, startChan, numChans, resynth, bases, basesMode, activations, actMode, components, iterations, windowSize, hopSize, fftSize, trig, blocking);
    }
    
    *process { |server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth = -1, bases = -1, basesMode = 0, activations = -1, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1,freeWhenDone = true, action|

        source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};        
        resynth = resynth ? -1;
        bases = bases ? -1;
        activations = activations ? -1;
        
       ^this.new(
            server,nil,[resynth, bases, activations].select{|x| x!= -1}     
        ).processList([source, startFrame, numFrames, startChan, numChans, resynth, bases, basesMode, activations, actMode, components,iterations, windowSize, hopSize, fftSize,0],freeWhenDone,action); 
    }
    
    *processBlocking { |server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, resynth = -1, bases = -1, basesMode = 0, activations = -1, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1,freeWhenDone = true, action|

        source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};        
        resynth = resynth ? -1;
        bases = bases ? -1;
        activations = activations ? -1;
        
       ^this.new(
            server,nil,[resynth, bases, activations].select{|x| x!= -1}     
        ).processList([source, startFrame, numFrames, startChan, numChans, resynth, bases, basesMode, activations, actMode, components,iterations, windowSize, hopSize, fftSize, 1],freeWhenDone,action); 
    }
}
