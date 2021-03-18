FluidBufSTFT : FluidBufProcessor {

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse = 0,windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 1|
        
        // source = source.asUGenInput;

        // source.isNil.if {"FluidBufScale:  Invalid source buffer".throw};
        source = source ? -1; 
        magnitudeBuffer = magnitudeBuffer ? -1;     
        phaseBuffer = phaseBuffer ? -1; 
        resynthesisBuffer = resynthesisBuffer ? - 1;     
        
        ^FluidProxyUgen.kr(\FluidBufSTFTTrigger, -1, source, startFrame, numFrames, startChan, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse, windowSize, hopSize, fftSize,trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|
        
        // source = source.asUGenInput;

        // source.isNil.if {"FluidBufSTFT:  Invalid source buffer".throw};
        source = source ? -1; 
        magnitudeBuffer = magnitudeBuffer ? -1;     
        phaseBuffer = phaseBuffer ? -1; 
        resynthesisBuffer = resynthesisBuffer ? - 1;     
        
	    ^this.new(
            server, nil, [magnitudeBuffer,phaseBuffer,resynthesisBuffer].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse, windowSize, hopSize, fftSize, 0], freeWhenDone, action
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1,freeWhenDone = true, action|
        
        // source = source.asUGenInput;
        source = source ? -1; 
        magnitudeBuffer = magnitudeBuffer ? -1;     
        phaseBuffer = phaseBuffer ? -1; 
        resynthesisBuffer = resynthesisBuffer ? - 1; 
        
	    ^this.new(
            server, nil, [magnitudeBuffer,phaseBuffer,resynthesisBuffer].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitudeBuffer, phaseBuffer, resynthesisBuffer, inverse, windowSize, hopSize, fftSize,1], freeWhenDone, action
		);
	}
}
