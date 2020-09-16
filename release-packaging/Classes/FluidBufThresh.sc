FluidBufThresh : UGen {

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, threshold = 0, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufThresh:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufThresh:  Invalid destination buffer".throw};
		^super.new1(rate, source, startFrame, numFrames, startChan, numChans,  destination, threshold, trig, blocking);
	}

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, threshold = 0, trig = 1, blocking = 1|
        ^this.new1('control', source, startFrame, numFrames, startChan, numChans, destination, threshold, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, destination, threshold
		);

	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1,  destination, threshold = 0, action|
    ^process(
	   source, startFrame, numFrames, startChan, numChans, destination, threshold
    );
	}
}
