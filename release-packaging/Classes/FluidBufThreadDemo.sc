FluidBufThreadDemo : FluidBufProcessor{

	*kr  {|result, time, trig = 1, blocking = 0|

		result = this.validateBuffer(result, "result");

		^FluidProxyUgen.kr(\FluidBufThreadDemoTrigger, -1, result, time, trig, blocking);
	}

	*process { |server, result, time = 1000, freeWhenDone = true, action|

		result = this.validateBuffer(result, "result");

		^this.new(
			server, nil, [result]
		).processList(
			[result.asUGenInput, time, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, result, time = 1000, freeWhenDone = true, action|

		result = this.validateBuffer(result, "result");

		^this.new(
			server, nil, [result]
		).processList(
			[result.asUGenInput, time, 1], freeWhenDone, action
		);
	}
}
FluidBufThreadDemoTrigger : FluidProxyUgen {}
