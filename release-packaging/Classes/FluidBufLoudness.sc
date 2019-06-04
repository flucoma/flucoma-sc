FluidBufLoudness{
		*process { arg server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, action;

		var maxwindowSize = windowSize.nextPowerOfTwo;

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"FluidBufPitch:  Invalid source buffer".throw};
		features.isNil.if {"FluidBufPitch:  Invalid features buffer".throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufLoudness, source, startFrame, numFrames, startChan, numChans, features, kWeighting, truePeak, windowSize, hopSize, maxwindowSize);
			server.sync;
			features = server.cachedBufferAt(features); features.updateInfo; server.sync;
			action.value(features);
		};
	}
}
