FluidStats : MultiOutUGen {

    var <channels;

	*kr { arg inputs;
        // ^super.new.rate_('control').inputs_(inputs.asArray++0).initOutputs(inputs.asArray.size);
        ^this.new1('control',inputs);
	}

	init {arg ...theInputs;
		inputs = theInputs ++ 0;
		^this.initOutputs(inputs.asArray.size - 1,rate);
	}

	checkInputs {
/*		if(inputs.any.rate != 'control') {
			^"input array input is not control rate";
		};*/
        this.specialIndex = (inputs.size - 2).min(0);
        this.specialIndex.postln;
		^this.checkValidInputs;
	}

    initOutputs{|numChans,rate|
        if(numChans.isNil or: {numChans < 1})
        {
            Error("No input channels").throw
        };

        channels = Array.fill(numChans * 2, { |i|
            // Array.fill(numChans,{ |j|
            OutputProxy('control',this,i);
        });
// });
        ^channels;
    }

    // synthIndex_ { arg index;
    //     synthIndex = index;
    //     channels.do{
    //         arg outputGroup;
    //         outputGroup.do{
    //             arg output;
    //             output.synthIndex_(index);
    //         }
    //     }
    // }


    numOutputs { ^(channels.size); }
}
