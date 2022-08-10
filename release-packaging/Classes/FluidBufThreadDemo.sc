FluidBufThreadDemo : FluidBufProcessor{

	*kr  {|result, time, trig = 1, blocking = 0|

		result = result.asUGenInput;
		result.isNil.if {this.class.name+":  Invalid output buffer".throw};

		^FluidProxyUgen.kr(\FluidBufThreadDemoTrigger, -1, result, time, trig, blocking);
	}

	*process { |server, result, time = 1000, freeWhenDone = true, action|


		result ?? {this.class.name+":  Invalid output buffer".throw};

		^this.new(
			server, nil, [result]
		).processList(
			[result.asUGenInput, time, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, result, time = 1000, freeWhenDone = true, action|

		result ?? {this.class.name+":  Invalid output buffer".throw};

		^this.new(
			server, nil, [result]
		).processList(
			[result.asUGenInput, time, 1], freeWhenDone, action
		);
	}
}
FluidBufThreadDemoTrigger : FluidProxyUgen {}
