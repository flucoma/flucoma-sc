FluidBufThreadDemo : UGen{

    *new1 {|rate, result, time, trig = 1, blocking = 0 |
		result = result.asUGenInput;
		result.isNil.if {this.class.name+":  Invalid output buffer".throw};
        ^super.new1(rate, result, time, trig, blocking);
	 }


	*kr  {|result, time, trig = 1, blocking = 0| 
        ^this.new1(\control, result, time, trig, blocking);
	 }

    *process { |server, result, time = 1000, action|
		^FluidNRTProcess.new(
			server, this, action, [result]
		).process(
			result, time
		);
    }

    *processBlocking { |server, result, time = 1000, action|
		^FluidNRTProcess.new(
			server, this, action, [result], blocking: 1
		).process(
			result, time
		);
    }
}
