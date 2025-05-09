FluidConcat : FluidRTUGen {
	*ar { arg sourceIn, controlSegmentTrig, controlFeatureTrig, sourceSegmentTrig, sourceFeatureTrig, 
				controlFeatureBuffer, sourceFeatureBuffer, maxHistoryLength=5000, historyWindowLength=3000, historyWindowOffset=0, fadeTime=100, speed=1, algo=0, randomness=0;
		^this.multiNew('audio', sourceIn.asAudioRateInput(this), 
		controlSegmentTrig.asAudioRateInput(this), 
		controlFeatureTrig.asAudioRateInput(this), 
		sourceSegmentTrig.asAudioRateInput(this), 
		sourceFeatureTrig.asAudioRateInput(this), 
		controlFeatureBuffer.asUGenInput, sourceFeatureBuffer.asUGenInput,
		maxHistoryLength, historyWindowLength, historyWindowOffset, fadeTime, speed, algo, randomness)
	}
	
	
	checkInputs {
		// the checks of rates here are in the order of the kr method definition
//		if(inputs.at(2).rate != 'scalar') {
//^(": maxNumCoeffs cannot be modulated.");
//		};
		^this.checkValidInputs;
	}

}

