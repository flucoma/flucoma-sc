FluidBufFlatten : FluidBufProcessor {


    *kr  { |source, destination, axis = 1, trig = 1, blocking = 1| 
        
        source = source.asUGenInput;
        destination = destination.asUGenInput;

        source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
        destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};
        
        ^FluidProxyUgen.kr(\FluidBufFlattenTrigger,-1,  source, destination, axis, trig, blocking);
	}

	*process { |server, source,  destination, axis = 1, freeWhenDone = true, action|
        
        source = source.asUGenInput;
        destination = destination.asUGenInput;

        source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
        destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};
        
	^this.new(
            server, nil, [destination],
		).processList(
			[source, destination, axis,0],freeWhenDone,action
		);

	}

   *processBlocking  { |server, source,  destination, axis = 1, freeWhenDone = true, action|
       
       source = source.asUGenInput;
       destination = destination.asUGenInput;

       source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
       destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};
       
   ^this.new(
           server, nil, [destination],
       ).processList(
           [source, destination, axis,1],freeWhenDone,action
       );

   }
}
