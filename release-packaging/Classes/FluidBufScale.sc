FluidBufScale : UGen {

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufScale:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufScale:  Invalid destination buffer".throw};
		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh, trig, blocking);
	}

    *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, trig = 1, blocking = 1|
        ^this.new1('control', source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh
		);

	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, destination, inputLow = 0, inputHigh = 1, outputLow = 0, outputHigh = 1, action|
    ^process(
	   source, startFrame, numFrames, startChan, numChans, destination, inputLow, inputHigh, outputLow, outputHigh
    );
	}
}
