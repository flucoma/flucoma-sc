FluidBufLoudness : FluidBufProcessor{
    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, trig = 1, blocking = 0| 
        
        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};
            
        ^FluidProxyUgen.kr(\FluidBufLoudnessTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, trig, blocking);
    }

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, freeWhenDone = true, action|
        
        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};
            
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,0],freeWhenDone,action
		);
    }

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, freeWhenDone = true, action|
        
        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};
            
		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,1],freeWhenDone,action
		);
    }
}
