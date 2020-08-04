FluidBufLoudness : UGen{
    *new1 { |rate,source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, trig = 1, blocking = 0|

        var maxwindowSize = windowSize.nextPowerOfTwo;

        source = source.asUGenInput;
        features = features.asUGenInput;

        source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
        features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

        ^super.new1(rate, source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, trig, blocking);
    }

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, trig = 1, blocking = 0| 
        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, trig, blocking );
    }

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, action|
		^FluidNRTProcess.new(
			server, this, action, [features]
		).process(
			source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize
		);
    }

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, action|
		^FluidNRTProcess.new(
			server, this, action, [features], blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize
		);
    }
}
