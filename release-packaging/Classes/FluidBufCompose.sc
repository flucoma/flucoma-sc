FluidBufCompose{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, action;

		source = source.asUGenInput;
		destination = destination.asUGenInput;

		if(source.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};
		if(destination.isNil) {Error("Invalid Buffer").format(thisMethod.name, this.class.name).throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufCompose, source, startFrame, numFrames, startChan, numChans, gain, destination, destStartFrame, destStartChan, destGain);
			server.sync;
			destination = server.cachedBufferAt(destination); destination.updateInfo; server.sync;
			action.value(destination);
		};
	}
}
