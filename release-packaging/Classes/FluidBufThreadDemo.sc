FluidBufThreadDemo : UGen{
    var <>synth, <>server;

		*kr {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 0, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)
        ^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, features, algorithm, minFreq, maxFreq, unit, windowSize, hopSize, fftSize, maxFFTSize, doneAction);
	}

    *process { |server, result, time, action|

		var synth,instance;

		result = result.asUGenInput;

		result.isNil.if {"FluidBufThreadDemo:  Invalid source buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });

        synth = { instance = FluidBufPitch.kr(result, time, doneAction:Done.freeSelf)}.play(server);

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
