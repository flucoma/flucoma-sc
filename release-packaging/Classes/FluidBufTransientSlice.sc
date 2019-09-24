FluidBufTransientSlice : UGen{
    var <>synth, <>server;

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, doneAction = 0|

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength, doneAction);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, minSliceLength = 1000, action|

        var synth, instance;
		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        synth = {instance = FluidBufTransientSlice.kr(source, startFrame, numFrames, startChan, numChans, indices, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, minSliceLength, doneAction: Done.freeSelf)}.play(server);

		forkIfNeeded{
            synth.waitForFree;
			server.sync;
			indices = server.cachedBufferAt(indices); indices.updateInfo; server.sync;
			action.value(indices);
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
