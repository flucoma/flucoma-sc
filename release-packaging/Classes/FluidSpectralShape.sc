FluidSpectralShape : FluidRTMultiOutUGen {

	const <features=#[\centroid,\spread,\skewness,\kurtosis,\rolloff,\flatness,\crest];
	classvar featuresLookup;

	*initClass {
		featuresLookup = Dictionary.with(*this.features.collect{|x,i| x->(1<<i)});
	}

	*prWarnUnrecognised {|sym| ("WARNING: FluidSpectralShape -" + sym + "is not a recognised option").postln}

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


	*kr { arg in = 0, select, minFreq = 0, maxFreq = -1, rolloffPercent = 95, unit = 0, power = 0, windowSize = 1024, hopSize = -1, fftSize = -1, maxFFTSize = -1;

		var selectbits  =  select !? {this.prProcessSelect(select)} ?? {this.prProcessSelect(this.features)};

		^this.multiNew('control', in.asAudioRateInput(this), selectbits, minFreq, maxFreq, rolloffPercent, unit, power, windowSize, hopSize, fftSize, maxFFTSize);
	}

	init {arg ...theInputs;
		var numChannels;
		inputs = theInputs;
		numChannels = inputs.at(1).asBinaryDigits.sum;
		^this.initOutputs(numChannels,rate);
	}

	checkInputs {
		if(inputs.at(10).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
		^this.checkValidInputs;
	}
}
