FluidBufThresh : UGen {

    *new1 { |rate, source, destination, threshold = 0, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};
		^super.new1(rate, source,  destination, threshold, trig, blocking);
	}

    *kr  { |source, destination, threshold = 0, trig = 1, blocking = 1| 
        ^this.new1('control', source, destination, threshold, trig, blocking);
	}

	*process { |server, source,  destination, threshold = 0, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, destination, threshold
		);

	}

   *processBlocking { |server, source,  destination, threshold = 0, action|
    ^process(
	   source, destination, threshold
    );
	}
}
