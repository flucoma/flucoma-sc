FluidConcat : FluidRTUGen {
	*ar { arg in, segmentTrig, featureTrig, featureBuffer, maxHistoryLength=5000, historyWindowLength=3000, historyWindowOffset=0, fadeTime=100, speed=1, algo=0;
		^this.multiNew('audio', in.asAudioRateInput(this), segmentTrig.asAudioRateInput(this), featureTrig.asAudioRateInput(this),featureBuffer.asUGenInput,	maxHistoryLength, historyWindowLength, historyWindowOffset, fadeTime, speed, algo)
	}
	
//	*ar { arg in, segmentTrig, test;
	//	^this.multiNew('audio', in.asAudioRateInput(this), segmentTrig, test)
	//}
	
	checkInputs {
		// the checks of rates here are in the order of the kr method definition
//		if(inputs.at(2).rate != 'scalar') {
//^(": maxNumCoeffs cannot be modulated.");
//		};
		^this.checkValidInputs;
	}

}

