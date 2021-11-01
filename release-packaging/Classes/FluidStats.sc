FluidStats : MultiOutUGen {

    var <channels;

	*kr { arg inputsArray, size;
        ^this.multiNew('control',*(inputsArray.asArray++size));
	}

	init {arg ...theInputs;
		inputs = theInputs;
        this.specialIndex = (inputs.size - 2).max(0);
		// this.specialIndex.postln;
		^this.initOutputs(inputs.size - 1,rate);
	}

	checkInputs {
/*		if(inputs.any.rate != 'control') {
			^"input array input is not control rate";
		};*/

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
        ^channels.reshape(2,numChans);
    }

    numOutputs { ^(channels.size); }
}
