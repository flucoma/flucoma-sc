FluidBufCompose : UGen {

    *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, trig = 1, blocking|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufCompose:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufCompose:  Invalid destination buffer".throw};

        ^super.new1(rate, source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, trig, blocking);
	}

/*    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, trig = 1|

        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, trig, blocking:1);
	}*/


	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, action|
	^FluidNRTProcess.new(
            server, this, action, [destination], blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain
		);

	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, action|
    ^process(
	   source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain
      );
	}
}
