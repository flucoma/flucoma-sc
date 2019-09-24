FluidNRTProcess : Object{
    var  <server, <ugen, <action, <outputBuffers, <synth;

	*new {|server, ugen, action, outputBuffers|
		^super.newCopyArgs(server, ugen, action, outputBuffers).init;
	}

	init{
		server = server ? Server.default;
		server.ifNotRunning({
            "FluidNRTProcess: Server not running".throw;
        });
		if (ugen.isNil){
			"FluidNRTProcess: UGen is nil".throw;
		};
		outputBuffers = outputBuffers.collect{|b|
			var checked = server.cachedBufferAt(b.asUGenInput);
			checked.isNil.if{ (ugen.asString ++":" + "Invalid buffer").throw};
			checked
		}
		^this;
	}

	process{|...ugenArgs|
		synth = {
            ugen.performList(\kr, ugenArgs.drop(-1).collect{|a| a.asUGenInput} ++ Done.freeSelf ++ ugenArgs.keep(-1).collect{|a| a.asUGenInput})
		}.play(server);
		synth.postln;
		forkIfNeeded{
            synth.waitForFree;
			server.sync;
            "HERE".postln;
			outputBuffers.do{|buf|
				buf = server.cachedBufferAt(buf.asUGenInput);
				buf.updateInfo;
				server.sync;
			};
			if(action.notNil){action.valueArray(outputBuffers)};
		};
		^this;
	}

	cancel{
		if(server.notNil && synth.notNil) {synth.free};
	}
}
