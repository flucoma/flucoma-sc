FluidVoiceAllocator : MultiOutUGen {

	*kr { arg in, in2, in3, history = 1;
		^this.multiNew('control',*(in.asArray++in2.asArray++in3.asArray++history)).reshape(4,in.asArray.size).postln;
	}

	init {arg ...theInputs;
		inputs = theInputs;
		inputs.postln;
		inputs.size.postln;
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

		channels = Array.fill(numChans, { |i|
			OutputProxy('control',this,i);
		});
		^channels
	}

	numOutputs { ^(channels.size); }
}

