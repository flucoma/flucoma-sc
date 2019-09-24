FluidBufThreadDemo : UGen{

	*kr {|result, time, doneAction = 0|
		result = result.asUGenInput;
		result.isNil.if {this.class.name+":  Invalid output buffer".throw};
        ^this.multiNew(\control, result, time, doneAction);
	 }

    *process { |server, result, time = 1000, action|
		^FluidNRTProcess.new(
			server, this, action, [result]
		).process(
			result, time
		);
    }
}
