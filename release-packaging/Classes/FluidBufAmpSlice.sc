FluidBufAmpSlice : UGen {

    var <>server, <>synth;

	*kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, doneAction = 0|

		var maxSize = max(minLengthAbove + lookBack, max(minLengthBelow,lookAhead));

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, maxSize, 0, doneAction);
	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, absRampUp = 10, absRampDown = 10, absThreshOn = -90, absThreshOff = -90, minSliceLength = 1, minSilenceLength = 1, minLengthAbove = 1, minLengthBelow = 1, lookBack = 0, lookAhead = 0, relRampUp = 1, relRampDown = 1, relThreshOn = 144, relThreshOff = -144, highPassFreq = 85, action|

        var instance,synth;

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufAmpSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufAmpSlice:  Invalid features buffer".throw};

		server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        synth = {instance = FluidBufAmpSlice.kr(source, startFrame, numFrames, startChan, numChans, indices, absRampUp, absRampDown, absThreshOn, absThreshOff, minSliceLength, minSilenceLength, minLengthAbove, minLengthBelow, lookBack, lookAhead, relRampUp, relRampDown, relThreshOn, relThreshOff, highPassFreq, doneAction: Done.freeSelf)}.play(server);

         forkIfNeeded{
            synth.waitForFree;
			server.sync;
			indices = server.cachedBufferAt(indices);
            indices.updateInfo;
            server.sync;
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
