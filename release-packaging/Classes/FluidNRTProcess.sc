FluidNRTProcess : Object{
	var  <server, <ugen, <action, <outputBuffers, <blocking, <synth;

	*new {|server, ugen, action, outputBuffers, blocking = 0|
		^super.newCopyArgs(server, ugen, action, outputBuffers, blocking).init;
	}

	init{
		server = server ? Server.default;
		server.ifNotRunning({
			"FluidNRTProcess: Server not running".throw;
		});
		if (ugen.isNil){
			"FluidNRTProcess : FluidRTUGen is nil".throw;
		};
		outputBuffers = outputBuffers.collect{|b|
			var checked = server.cachedBufferAt(b.asUGenInput);
			checked.isNil.if{ (ugen.asString ++":" + "Invalid buffer").throw};
			checked
		}
		^this;
	}


	process{|...ugenArgs|

		var c = Condition.new(false);

		synth = {
			FreeSelfWhenDone.kr(ugen.performList(\new1,\control, ugenArgs.collect{|a| a.asUGenInput} ++ 1 ++ blocking));
		}.play(server);
		synth.postln;

		OSCFunc({ |m|
			forkIfNeeded{
				outputBuffers.do{|buf|
					buf = server.cachedBufferAt(buf.asUGenInput);
					buf.updateInfo;
				};
				server.sync;
				if(action.notNil && m[2]==0){action.valueArray(outputBuffers)};
				c.test = true;
				c.signal;
			}
		},'/done', srcID:server.addr, argTemplate:[synth.nodeID]).oneShot;

		forkIfNeeded{
			c.wait;
		};
		^this;
	}

	cancel{
		if(server.notNil && synth.notNil) {synth.free};
	}
}
