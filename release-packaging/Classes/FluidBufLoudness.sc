FluidBufLoudness{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, winSize = 1024, hopSize = 512, action;

		var maxWinSize = winSize.nextPowerOfTwo;

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufLoudness, source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, winSize, hopSize, maxWinSize);
			server.sync;
			features = server.cachedBufferAt(features); features.updateInfo; server.sync;
			action.value(features);
		};
	}
}
