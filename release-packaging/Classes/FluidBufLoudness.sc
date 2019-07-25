FluidBufLoudness : UGen{

    var <>server, <>synth;

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, doneAction = 0|

        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, doneAction);
    }

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, action|

        var synth, instance;
        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

        server = server ? Server.default;
        server.ifNotRunning({
            "WARNING: Server not running".postln;
            ^nil;
        });
        synth = {instance = FluidBufLoudness.kr(source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, doneAction:Done.freeSelf)}.play(server);

        forkIfNeeded{
            synth.waitForFree;
            server.sync;
            features = server.cachedBufferAt(features); features.updateInfo; server.sync;
            action.value(features);
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
