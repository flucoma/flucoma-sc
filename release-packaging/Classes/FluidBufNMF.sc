FluidBufNMF : UGen {

    *kr {|source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, bases, basesMode = 0, activations, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1, doneAction = 0|

        source = source.asUGenInput;
        destination = destination.asUGenInput;
        bases = bases.asUGenInput;
        activations = activations.asUGenInput;

        source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};

        destination = destination ? -1;
        bases = bases ? -1;
        activations = activations ? -1;

        ^this.multiNew('control',source, startFrame, numFrames, startChan, numChans, destination, bases, basesMode, activations, actMode, components, iterations, windowSize, hopSize, fftSize, doneAction);

    }


    *process { |server,  source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, bases, basesMode = 0, activations, actMode = 0, components = 1, iterations = 100, windowSize = 1024, hopSize = -1, fftSize = -1, windowType = 0, randomSeed = -1, action|

        var synth;
        source = source.asUGenInput;
        destination = destination.asUGenInput;
        bases = bases.asUGenInput;
        activations = activations.asUGenInput;

        source.isNil.if {"FluidBufNMF:  Invalid source buffer".throw};

        destination = destination ? -1;
        bases = bases ? -1;
        activations = activations ? -1;

        synth = {FluidBufNMF.kr(source, startFrame, numFrames, startChan, numChans, destination, bases, basesMode, activations, actMode, components, iterations, windowSize, hopSize, fftSize, doneAction: Done.freeSelf)}.play(server);

        synth.waitForFree({ forkIfNeeded{
            server.sync;
            if (destination != -1) {
                destination = server.cachedBufferAt(destination);
                destination.updateInfo;
                server.sync;
            } {destination = nil};
            if (bases != -1) {
                bases = server.cachedBufferAt(bases);
                bases.updateInfo;
                server.sync;
            } {bases = nil};
            if (activations != -1) {
                activations = server.cachedBufferAt(activations);
                activations.updateInfo;
                server.sync;
            } {activations = nil};
            action.value(destination, bases, activations);
        }});
    }
}
