FluidBufCompose : UGen {

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, doneAction = 0|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufCompose:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufCompose:  Invalid destination buffer".throw};

        ^this.multiNew('control', source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain, doneAction);
	}


	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, action|

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		source.isNil.if {"FluidBufCompose:  Invalid source buffer".throw};
		destination.isNil.if {"FluidBufCompose:  Invalid destination buffer".throw};

	^FluidNRTProcess.new(
			server, this, action, [destination]
		).process(
			source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain
		);

	}

}
