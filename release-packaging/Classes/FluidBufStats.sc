FluidBufStats : FluidBufProcessor {

	const <stats=#[\mean,\std,\skewness,\kurtosis,\low,\mid,\high];
	classvar statslookup;

	*prWarnUnrecognised {|sym| ("WARNING: FluidBufStats -" + sym + "is not a recognised option").postln}

	*prProcessSelect {|a|
		var bits;
		a.asBag.countsDo{|item,count,i|
			if(count > 1) { ("Option '" ++ item ++ "' is repeated").warn};
		};
		bits = a.collect{ |sym|
			(statslookup[sym.asSymbol] !? {|x| x} ?? {this.prWarnUnrecognised(sym); 0})
		}.reduce{|x,y| x | y};
		^bits
	}

	*initClass {
		statslookup = Dictionary.with(*this.stats.collect{|x,i| x->(1<<i)});
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, trig = 1, blocking = 0|

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.stats)};
		source = source.asUGenInput;
		stats = stats.asUGenInput;
		weights = weights.asUGenInput;

		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};
		weights = weights ? -1;

		^FluidProxyUgen.kr(\FluidBufStatsTrigger, -1, source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select, numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, freeWhenDone = true, action|

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.stats)};
		source = source.asUGenInput;
		stats = stats.asUGenInput;
		weights = weights.asUGenInput;

		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};
		weights = weights ? -1;

		^this.new(
			server, nil, [stats]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, 0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, stats, select numDerivs = 0, low = 0, middle = 50, high = 100, outliersCutoff = -1, weights, freeWhenDone = true, action|

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.stats)};
		source = source.asUGenInput;
		stats = stats.asUGenInput;
		weights = weights.asUGenInput;
		source.isNil.if {"FluidBufStats:  Invalid source buffer".throw};
		stats.isNil.if {"FluidBufStats:  Invalid stats buffer".throw};
		weights = weights ? -1;

		^this.new(
			server, nil, [stats]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, stats, selectbits, numDerivs, low, middle, high, outliersCutoff, weights, 1], freeWhenDone, action
		);
	}


}
FluidBufStatsTrigger : FluidProxyUgen {}
