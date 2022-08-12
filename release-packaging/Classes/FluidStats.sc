FluidStats : MultiOutUGen {

	*kr { arg in, history;
		^this.multiNew('control',*(in.asArray++history)).reshape(2,in.asArray.size);
	}

	init {arg ...theInputs;
		inputs = theInputs;
		this.specialIndex = (inputs.size - 2).max(0);
		^this.initOutputs(inputs.size - 1,rate)
	}

	checkInputs {
		^this.checkValidInputs;
	}

	initOutputs{|numChans,rate|
		if(numChans.isNil or: {numChans < 1})
		{
			Error("No input channels").throw
		};

		channels = Array.fill(numChans * 2, { |i|
			OutputProxy('control',this,i);
		});
		^channels
	}

	numOutputs { ^(channels.size); }
}
