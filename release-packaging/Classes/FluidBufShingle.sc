FluidBufShingle : FluidBufProcessor {

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, size = 2, trig = 1, blocking = 1|

        source = source.asUGenInput;
        destination = destination.asUGenInput;

        source.isNil.if {"FluidBufShingle:  Invalid source buffer".throw};
        destination.isNil.if {"FluidBufShingle:  Invalid destination buffer".throw};

        ^FluidProxyUgen.kr(\FluidBufShingleTrigger,-1,  source, startFrame, numFrames, startChan, numChans, destination, size, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, size = 2, freeWhenDone = true, action|

        source = source.asUGenInput;
        destination = destination.asUGenInput;

        source.isNil.if {"FluidBufShingle:  Invalid source buffer".throw};
        destination.isNil.if {"FluidBufShingle:  Invalid destination buffer".throw};

	^this.new(
            server, nil, [destination],
		).processList(
			[source, startFrame, numFrames, startChan, numChans, destination, size,0],freeWhenDone,action
		);

	}

   *processBlocking  { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, size = 2, freeWhenDone = true, action|

       source = source.asUGenInput;
       destination = destination.asUGenInput;

       source.isNil.if {"FluidBufShingle:  Invalid source buffer".throw};
       destination.isNil.if {"FluidBufShingle:  Invalid destination buffer".throw};

   ^this.new(
           server, nil, [destination],
       ).processList(
           [source, startFrame, numFrames, startChan, numChans, destination, size,1],freeWhenDone,action
       );

   }
}
FluidBufShingleTrigger : FluidProxyUgen {}
