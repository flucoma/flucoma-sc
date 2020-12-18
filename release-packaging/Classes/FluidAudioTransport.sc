FluidAudioTransport : UGen {

    init { |...theInputs|
        theInputs;
        inputs = theInputs;
        this.specialIndex = 1; //two audio inputs
        // ^this.initOutputs(1,rate);
    }

	*ar { arg in = 0, in2 = 0 , interpolation=0.0, bandwidth=255,windowSize= 1024, hopSize= -1, fftSize= -1, maxFFTSize = 16384;
		^this.multiNew('audio', in.asAudioRateInput,  in2, interpolation, bandwidth, windowSize, hopSize, fftSize, maxFFTSize)
	}
}