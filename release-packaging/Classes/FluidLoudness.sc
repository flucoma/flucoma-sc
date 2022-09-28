FluidLoudness : FluidRTMultiOutUGen {

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

	*kr { arg in = 0, select, kWeighting = 1, truePeak = 1, windowSize = 1024, hopSize = 512, maxWindowSize = 16384;

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.features)};

		^this.multiNew('control', in.asAudioRateInput(this), selectbits, kWeighting, truePeak, windowSize, hopSize, maxWindowSize);
	}

	init {arg ...theInputs;
		var numChannels;
		inputs = theInputs;
		numChannels = inputs.at(1).asBinaryDigits.sum;
		^this.initOutputs(numChannels,rate);
	}

	checkInputs {
		if(inputs.at(6).rate != 'scalar') {
			^(": maxwindowSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
