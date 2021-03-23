FluidBufSTFT : FluidBufProcessor {

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynthesis, inverse = 0,windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 1|

        // source = source.asUGenInput;

        // source.isNil.if {"FluidBufScale:  Invalid source buffer".throw};
        source = source ? -1;
        magnitude = magnitude ? -1;
        phase = phase ? -1;
        resynthesis = resynthesis ? - 1;

        ^FluidProxyUgen.kr(\FluidBufSTFTTrigger, -1, source, startFrame, numFrames, startChan, magnitude, phase, resynthesis, inverse, windowSize, hopSize, fftSize,trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynthesis, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|

        // source = source.asUGenInput;

        // source.isNil.if {"FluidBufSTFT:  Invalid source buffer".throw};
        source = source ? -1;
        magnitude = magnitude ? -1;
        phase = phase ? -1;
        resynthesis = resynthesis ? - 1;

	    ^this.new(
            server, nil, [magnitude,phase,resynthesis].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitude, phase, resynthesis, inverse, windowSize, hopSize, fftSize, 0], freeWhenDone, action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynthesis, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|

        // source = source.asUGenInput;
        source = source ? -1;
        magnitude = magnitude ? -1;
        phase = phase ? -1;
        resynthesis = resynthesis ? - 1;

	    ^this.new(
            server, nil, [magnitude,phase,resynthesis].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitude, phase, resynthesis, inverse, windowSize, hopSize, fftSize,1], freeWhenDone, action
		);
	}
}
