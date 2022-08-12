FluidBufLoudness : FluidBufProcessor{

	const <features=#[\loudness, \peak];
	classvar featuresLookup;

	*initClass {
		featuresLookup = Dictionary.with(*this.features.collect{|x,i| x->(1<<i)});
	}

	*prWarnUnrecognised {|sym| ("WARNING: FluidLoudness -" + sym + "is not a recognised option").postln}

	*prProcessSelect {|a|
		var bits;
		a.asBag.countsDo{|item,count,i|
			if(count > 1) { ("Option '" ++ item ++ "' is repeated").warn};
		};
		bits = a.collect{ |sym|
			(featuresLookup[sym.asSymbol] !? {|x| x} ?? {this.prWarnUnrecognised(sym); 0})
		}.reduce{|x,y| x | y};
		^bits
	}

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select, kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, trig = 1, blocking = 0|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.features)};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"%:  Invalid source buffer".format(this.class.name).throw};
		features.isNil.if {"%:  Invalid features buffer".format(this.class.name).throw};

		^FluidProxyUgen.kr(\FluidBufLoudnessTrigger, -1, source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, freeWhenDone = true, action|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.features)};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"%:  Invalid source buffer".format(this.class.name).throw};
		features.isNil.if {"%:  Invalid features buffer".format(this.class.name).throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features, padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, features, select,  kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, padding = 1, freeWhenDone = true, action|

		var maxwindowSize = windowSize.nextPowerOfTwo;

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.features)};

		source = source.asUGenInput;
		features = features.asUGenInput;

		source.isNil.if {"%:  Invalid source buffer".format(this.class.name).throw};
		features.isNil.if {"%:  Invalid features buffer".format(this.class.name).throw};

		^this.new(
			server, nil, [features]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, features,padding, selectbits, kWeighting, truePeak, windowSize, hopSize, maxwindowSize,1],freeWhenDone,action
		);
	}
}
FluidBufLoudnessTrigger : FluidProxyUgen {}
