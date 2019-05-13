FluidBufStats{
		*process { arg server, source, stats, numDerivatives = 0, low = 0, middle = 50, high = 100, action;

		source = source.asUGenInput;
		stats = stats.asUGenInput;

		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};

		server = server ? Server.default;

		forkIfNeeded{
			server.sendMsg(\cmd, \BufStats, source, stats, numDerivatives, low, middle, high);
			server.sync;
			stats = server.cachedBufferAt(stats); stats.updateInfo; server.sync;
			action.value(stats);
		};
	}
}
