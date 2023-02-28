FluidVoiceAllocator : MultiOutUGen {

	*kr { arg in, in2, in3, history = 1;
		^this.multiNew('control',*(in.asArray++in2.asArray++in3.asArray++history)).reshape(3,in.asArray.size);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		this.specialIndex = (inputs.size - 2).max(0);
		^this.initOutputs(inputs.size - 1,rate)
	}

	checkInputs {
		if(((inputs.size - 1).mod(3)) != 0) {
		^(": the 3 array inputs must be of equal length.");
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


