FluidBufLoudness : UGen{

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, doneAction = 0, blocking = 0|

        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, doneAction, blocking);
    }

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, action, blocking = 0 |
		^FluidNRTProcess.new(
			server, this, action, [features]
		).process(
			source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, blocking
		);
    }
}
