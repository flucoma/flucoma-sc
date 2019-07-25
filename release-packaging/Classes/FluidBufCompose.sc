FluidBufCompose : UGen {

    var <>server, <>synth;

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, doneAction = 0|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufCompose:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufCompose:  Invalid destination buffer".throw};


        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, doneAction);

	}


	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, action|

        var synth, instance;
		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufCompose:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufCompose:  Invalid destination buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        synth = {instance = FluidBufCompose.kr(source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, doneAction: Done.freeSelf)}.play(server);

		forkIfNeeded{
            synth.waitForFree;
			server.sync;
			destination = server.cachedBufferAt(destination);
            destination.updateInfo;
            server.sync;
			action.value(destination);
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
