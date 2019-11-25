FluidBufAudioTransport : UGen{

    var <>synth, <>server;

    *kr { |source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
        source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};
		source1 = source1.asUGenInput;
        source2 = source2.asUGenInput;

        destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};
        destination = destination.asUGenInput;
		//NB For wrapped versions of NRT classes, we set the params for maxima to
		//whatever has been passed in language-side (e.g maxFFTSize still exists as a parameter for the server plugin, but makes less sense here: it just needs to be set to a legal value)

		^this.multiNew(\control, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame1, numFrames1, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize, doneAction);
	}


    *process { |server, source1, startFrame1 = 0, numFrames1 = -1, startChan1 = 0, numChans1 = -1, source2, startFrame2 = 0, numFrames2 = -1, startChan2 = 0, numChans2 = -1, destination, interpolation=0.0, bandwidth=255, windowSize = 1024, hopSize = -1, fftSize = -1, action|
        var synth, instance;


		source1.isNil.if {"FluidAudioTransport:  Invalid source 1 buffer".throw};
        source2.isNil.if {"FluidAudioTransport:  Invalid source 2 buffer".throw};

        destination.isNil.if {"FluidAudioTransport:  Invalid destination buffer".throw};


        source1 = source1.asUGenInput;
        source2 = source2.asUGenInput;
		destination = destination.asUGenInput;

		server = server ? Server.default;
         server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        synth = { instance = FluidBufSines.kr(source1, startFrame1, numFrames1, startChan1, numChans1, source2, startFrame1, numFrames1, startChan2, numChans2, destination, interpolation, bandwidth, windowSize, hopSize, fftSize, doneAction:Done.freeSelf)}.play(server);
		forkIfNeeded{
			synth.waitForFree;
			server.sync;
			if (destination != -1) {destination = server.cachedBufferAt(destination); destination.updateInfo; server.sync;} {destination = nil};
			action.value(destination);
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
