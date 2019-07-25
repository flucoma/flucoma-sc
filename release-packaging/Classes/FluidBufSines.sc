FluidBufSines : UGen{

    var <>synth, <>server;

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines, residual, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		sines = sines.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		sines = sines ? -1;
		residual = residual ? -1;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize, maxFFTSize, doneAction);
	}


    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, sines, residual, bandwidth = 76, threshold = 0.7, minTrackLen = 15, magWeight = 0.1, freqWeight = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action|
        var synth, instance;
		source = source.asUGenInput;
		sines = sines.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufSines:  Invalid source buffer".throw};

		server = server ? Server.default;
         server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
		sines = sines ? -1;
		residual = residual ? -1;
        synth = { instance = FluidBufSines.kr(source, startFrame, numFrames, startChan, numChans, sines, residual, bandwidth, threshold, minTrackLen, magWeight, freqWeight, windowSize, hopSize, fftSize, doneAction:Done.freeSelf)}.play(server);
		forkIfNeeded{
			synth.waitForFree;
			server.sync;
			if (sines != -1) {sines = server.cachedBufferAt(sines); sines.updateInfo; server.sync;} {sines = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(sines, residual);
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
