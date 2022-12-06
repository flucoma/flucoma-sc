FluidBufStats : FluidBufProcessor {

	const <stats=#[\mean,\std,\skewness,\kurtosis,\low,\mid,\high];
	classvar <statslookup;

	*initClass {
		statslookup = FluidProcessSelect(this, this.stats);
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, trig = 1, blocking = 0|

		var selectbits = this.statslookup.encode(select);
		source = this.validateBuffer(source, "source");
		stats = this.validateBuffer(stats, "stats");
		weights = weights.asUGenInput ? -1;

		^FluidProxyUgen.kr(\FluidBufStatsTrigger, -1, source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, freeWhenDone = true, action|

		var selectbits = this.statslookup.encode(select);
		source = this.validateBuffer(source, "source");
		stats = this.validateBuffer(stats, "stats");
		weights = weights.asUGenInput ? -1;

		^this.new(
			server, nil, [stats]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, freeWhenDone = true, action|

		var selectbits = this.statslookup.encode(select);
		source = this.validateBuffer(source, "source");
		stats = this.validateBuffer(stats, "stats");
		weights = weights.asUGenInput ? -1;

		^this.new(
			server, nil, [stats]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, 1], freeWhenDone, action
		);
	}


}
FluidBufStatsTrigger : FluidProxyUgen {}
