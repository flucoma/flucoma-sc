FluidSineFeature : FluidRTMultiOutUGen {
	*kr { arg in = 0, numPeaks = 10, detectionThreshold = -96, order = 0, freqUnit = 0, magUnit = 0, windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = -1, maxNumPeaks = nil;

		maxNumPeaks = maxNumPeaks ? numPeaks;

		^this.multiNew('control', in.asAudioRateInput(this), numPeaks, maxNumPeaks, detectionThreshold, order, freqUnit, magUnit, windowSize, hopSize, fftSize, maxFFTSize).reshape(2,maxNumPeaks)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(inputs.at(2),rate);//this instantiate the number of output from the maxNumPeaks in the multiNew order
	}

	checkInputs {
		if(inputs.at(8).rate != 'scalar') {
			^(": maxNumPeaks cannot be modulated.");
		};
		if(inputs.at(7).rate != 'scalar') {
			^(": maxFFTSize cannot be modulated.");
		};
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
