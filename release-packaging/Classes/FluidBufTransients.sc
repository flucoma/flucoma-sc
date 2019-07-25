FluidBufTransients : UGen {

    var <>synth, <>server;

	 *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients, residual, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, doneAction = 0 |

		source = source.asUGenInput;
		transients = transients.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

        transients = transients ? -1;
		residual = residual ? -1;

        ^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, doneAction);

	}

	 *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients, residual, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, action|

        var synth, instance;
		source = source.asUGenInput;
		transients = transients.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        transients = transients ? -1;
		residual = residual ? -1;

        synth = {instance = FluidBufTransients.kr(source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, doneAction:Done.freeSelf)}.play(server);

		forkIfNeeded{
            synth.waitForFree;
			server.sync;
			if (transients != -1) {transients = server.cachedBufferAt(transients); transients.updateInfo; server.sync;} {transients = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(transients, residual);
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
