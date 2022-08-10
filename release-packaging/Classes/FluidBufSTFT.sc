FluidBufSTFT : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynth, inverse = 0,windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, trig = 1, blocking = 1|

		source = source ? -1;
		magnitude = magnitude ? -1;
		phase = phase ? -1;
		resynth = resynth ? - 1;

		^FluidProxyUgen.kr(\FluidBufSTFTTrigger, -1, source, startFrame, numFrames, startChan, magnitude, phase, resynth, inverse, padding, windowSize, hopSize, fftSize, -1,  trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynth, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1, freeWhenDone = true, action|

		source = source ? -1;
		magnitude = magnitude ? -1;
		phase = phase ? -1;
		resynth = resynth ? - 1;

		^this.new(
			server, nil, [magnitude,phase,resynth].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitude, phase, resynth, inverse, padding, windowSize, hopSize, fftSize, -1,  0], freeWhenDone, action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, magnitude, phase, resynth, inverse = 0, windowSize = 1024, hopSize = -1, fftSize = -1, padding = 1,freeWhenDone = true, action|

		source = source ? -1;
		magnitude = magnitude ? -1;
		phase = phase ? -1;
		resynth = resynth ? - 1;

		^this.new(
			server, nil, [magnitude,phase,resynth].select{|b| b != -1}
		).processList(
			[source, startFrame, numFrames, startChan, magnitude, phase, resynth, inverse, padding, windowSize, hopSize, fftSize, -1, 1], freeWhenDone, action
		);
	}
}
