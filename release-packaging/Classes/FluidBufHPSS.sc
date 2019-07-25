FluidBufHPSS : UGen {

    var <>server, <>synth;

	*kr {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic, percussive, residual, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		harmonic = harmonic.asUGenInput;
		percussive = percussive.asUGenInput;
		residual = residual.asUGenInput;

		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;

		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, maxFFTSize, harmFilterSize, percFilterSize, doneAction);
	}

   *process {|server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, harmonic, percussive, residual, harmFilterSize = 17, percFilterSize = 31, maskingMode = 0, harmThreshFreq1 = 0.1, harmThreshAmp1 = 0, harmThreshFreq2 = 0.5, harmThreshAmp2 = 0, percThreshFreq1 = 0.1, percThreshAmp1 = 0, percThreshFreq2 = 0.5, percThreshAmp2 = 0, windowSize = 1024, hopSize = -1, fftSize = -1, action|

        var synth, instance;

		source = source.asUGenInput;
		harmonic = harmonic.asUGenInput;
		percussive = percussive.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufHPSS:  Invalid source buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
		harmonic = harmonic ? -1;
		percussive = percussive ? -1;
		residual = residual ? -1;

        synth = { instance = FluidBufHPSS.kr(source, startFrame, numFrames, startChan, numChans, harmonic, percussive, residual, harmFilterSize, percFilterSize, maskingMode, harmThreshFreq1, harmThreshAmp1, harmThreshFreq2, harmThreshAmp2, percThreshFreq1, percThreshAmp1, percThreshFreq2, percThreshAmp2, windowSize, hopSize, fftSize, doneAction:Done.freeSelf)}.play(server);

		forkIfNeeded{
			synth.waitForFree;
			server.sync;
			if (harmonic != -1) {harmonic = server.cachedBufferAt(harmonic); harmonic.updateInfo; server.sync;} {harmonic = nil};
			if (percussive != -1) {percussive = server.cachedBufferAt(percussive); percussive.updateInfo; server.sync;} {percussive = nil};
			if (residual != -1) {residual = server.cachedBufferAt(residual); residual.updateInfo; server.sync;} {residual = nil};
			action.value(harmonic, percussive, residual);
		};

        instance.server = server;
        instance.synth = synth;
        ^instance;
	}

    cancel{
        if(this.server.notNil)
        {this.server.sendMsg("/u_cmd", this.synth.nodeID, this.synthIndex, "cancel")};
    }

}
