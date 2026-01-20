FluidVoiceAllocator : MultiOutUGen {

	*kr { arg in, in2, numVoices = 1, prioritisedVoices = 0, birthLowThreshold = -24, birthHighThreshold = -60, minTrackLen = 1, trackMagRange = 15, trackFreqRange = 50, trackProb = 0.5, maxNumVoices;

		maxNumVoices = maxNumVoices ? numVoices;

		^this.multiNew('control',*(in.asArray ++ in2.asArray ++ numVoices ++ maxNumVoices ++ prioritisedVoices ++ birthLowThreshold ++ birthHighThreshold ++ minTrackLen ++ trackMagRange ++ trackFreqRange ++ trackProb)).reshape(3,maxNumVoices);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		this.specialIndex = (inputs.size - 10).max(0);
		^this.initOutputs(inputs[inputs.size - 8] * 3,rate);
	}

	checkInputs {
		if(((inputs.size - 9).mod(2)) != 0) {
		^(": the 2 input arrays must be of equal length.");
		};
		^this.checkValidInputs;
	}

	initOutputs{|numChans,rate|
		if(numChans.isNil or: {numChans < 1})
		{
			Error("No input channels").throw
		};

		channels = Array.fill(numChans, { |i|
			OutputProxy('control',this,i);
		});
		^channels
	}

	numOutputs { ^(channels.size); }
}

