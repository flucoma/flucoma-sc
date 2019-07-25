FluidBufNoveltySlice : UGen {

    var <>synth, <>server;

		*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0 |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize, doneAction);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|

        var synth, instance;
		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		server = server ? Server.default;
		server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });

        synth = { instance = FluidBufNoveltySlice.kr(source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize, doneAction: Done.freeSelf)}.play(server);

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
