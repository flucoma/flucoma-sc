FluidBufThreadDemo : UGen{

    *new1 {|rate, result, time, doneAction = 0, blocking = 0 |
		result = result.asUGenInput;
		result.isNil.if {this.class.name+":  Invalid output buffer".throw};
        ^super.new1(rate, result, time, doneAction, blocking);
	 }


	*kr {|result, time, doneAction = 0|
        ^this.new1(\control, result, time, doneAction);
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
