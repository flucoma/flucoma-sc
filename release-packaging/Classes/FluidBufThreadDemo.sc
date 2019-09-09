FluidBufThreadDemo : UGen{
    var <>synth, <>server;

		*kr {|source, result, time, doneAction = 0|

		result = result.asUGenInput;

		result.isNil.if {"FluidBufThreadDemo:  Invalid output buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)
        ^this.multiNew(\control, result, time, doneAction);
	}

    *process { |server, result, time, action|

		var synth,instance;

		result = result.asUGenInput;

		result.isNil.if {"FluidBufThreadDemo:  Invalid output buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });

        synth = { instance = FluidBufThreadDemo.kr(result, time, doneAction:Done.freeSelf)}.play(server);

		forkIfNeeded{
            synth.waitForFree;
			server.sync;
			result = server.cachedBufferAt(result); result.updateInfo; server.sync;
			action.value(result);
		};
        instance.synth = synth;
        instance.server = server;
        ^instance;
	}

    cancel{
        if(this.server.notNil)
        {this.server.sendMsg("/u_cmd", this.synth.nodeID, this.synthIndex, "cancel")};
    }
}
