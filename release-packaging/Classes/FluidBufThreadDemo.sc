FluidBufThreadDemo : UGen{

	*kr {|result, time, doneAction = 0, blocking = 0|
		result = result.asUGenInput;
		result.isNil.if {this.class.name+":  Invalid output buffer".throw};
        ^this.multiNew(\control, result, time, doneAction, blocking);
	 }

    *process { |server, result, time = 1000, action, blocking = 0 |
		^FluidNRTProcess.new(
			server, this, action, [result]
		).process(
			result, time, blocking
		);
    }
}
