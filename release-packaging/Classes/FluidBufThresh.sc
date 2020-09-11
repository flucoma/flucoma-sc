FluidBufThresh : UGen {

    *new1 { |rate, source, destination, thresh = 0, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};
		^super.new1(rate, source,  destination, thresh, trig, blocking);
	}

    *kr  { |source, destination, thresh = 0, trig = 1, blocking = 1| 
        ^this.new1('control', source, destination, thresh, trig, blocking);
	}

	*process { |server, source,  destination, thresh = 0, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, destination, thresh
		);

	}

   *processBlocking { |server, source,  destination, thresh = 0, action|
    ^process(
	   source, destination, thresh
    );
	}
}
