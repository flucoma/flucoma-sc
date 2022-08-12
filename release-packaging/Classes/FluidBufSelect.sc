FluidBufSelect : FluidBufProcessor {

	*kr  { |source, destination, indices=#[-1], channels=#[-1], trig = 1, blocking = 1|

		var params;

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		indices = indices.asArray;
		channels = channels.asArray;

		indices = [indices.size] ++ indices;
		channels = [channels.size] ++ channels;

		source.isNil.if {"FluidBufSelect:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelect:  Invalid destination buffer".throw};

		params = indices ++ channels ++ [trig, blocking]

		^FluidProxyUgen.kr(\FluidBufSelectTrigger,-1, source, destination, *params);
	}


	*process { |server, source, destination, indices=#[-1], channels=#[-1], freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufSelect:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelect:  Invalid destination buffer".throw};

		indices = indices.asArray;
		channels = channels.asArray;

		indices = [indices.size] ++ indices;
		channels = [channels.size] ++ channels;

		^this.new(server, nil, [destination]).processList([source, destination]++ indices ++ channels ++ [1], freeWhenDone, action);//NB always blocking
	}

	*processBlocking { |server, source, destination, indices=#[-1], channels=#[-1], freeWhenDone = true, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufSelect:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufSelect:  Invalid destination buffer".throw};

		indices = indices.asArray;
		channels = channels.asArray;

		indices = [indices.size] ++ indices;
		channels = [channels.size] ++ channels;


		^this.new(
			server, nil, [destination]
		).processList([source, destination]++ indices ++ channels ++ [1], freeWhenDone, action);//NB always blocking
	}
}
FluidBufSelectTrigger : FluidProxyUgen {}
