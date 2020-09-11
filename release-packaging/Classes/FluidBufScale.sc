FluidBufScale : UGen {

    *new1 { |rate, source, destination, inlo, inhi, outlo, outhi, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufScale:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufScale:  Invalid destination buffer".throw};
		^super.new1(rate, source,  destination, inlo, inhi, outlo, outhi, trig, blocking);
	}

    *kr  { |source, destination, inlo = 0 , inhi = 1, outlo = 0, outhi = 1, trig = 1, blocking = 1| 
        ^this.new1('control', source, destination, inlo, inhi, outlo, outhi, trig, blocking);
	}

	*process { |server, source,  destination, inlo = 0 , inhi = 1, outlo = 0, outhi = 1, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, destination, inlo, inhi, outlo, outhi
		);

	}

   *processBlocking { |server, source,  destination, inlo = 0 , inhi = 1, outlo = 0 , outhi = 1, action|
    ^process(
	   source, destination, inlo, inhi, outlo, outhi
    );
	}
}
