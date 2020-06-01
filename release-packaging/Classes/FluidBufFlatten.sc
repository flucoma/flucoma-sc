FluidBufFlatten : UGen {

    *new1 { |rate, source, destination, axis = 1, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufFlatten:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufFlatten:  Invalid destination buffer".throw};
		^super.new1(rate, source,  destination, axis, trig, blocking);
	}

    *kr { |source, destination, axis = 1, trig = 1|
        ^this.new1('control', source, destination, axis, trig, 1);
	}

	*process { |server, source,  destination, axis = 1, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, destination, axis
		);

	}

   *processBlocking { |server, source,  destination, axis =  1, action|
    ^process(
	   source, destination, axis
    );
	}
}
