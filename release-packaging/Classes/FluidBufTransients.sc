FluidBufTransients : UGen {


    	 *new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, trig = 1, blocking = 0 |

		source = source.asUGenInput;
		transients = transients.asUGenInput;
		residual = residual.asUGenInput;

		source.isNil.if {"FluidBufTransients:  Invalid source buffer".throw};

        ^super.new1(rate, source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, trig, blocking);
	}


	 *kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, trig = 1, blocking = 0| 
        ^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength, trig, blocking);

	}

	 *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, action|
		^FluidNRTProcess.new(
			server, this, action,[transients, residual].select{|x| x!= -1}
		).process(
			source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength
		);
	}

   *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, transients = -1, residual = -1, order = 20, blockSize = 256, padSize = 128, skew = 0, threshFwd = 2, threshBack = 1.1, windowSize = 14, clumpLength = 25, action|
		^FluidNRTProcess.new(
			server, this, action,[transients, residual].select{|x| x!= -1}, blocking: 1
		).process(
			source, startFrame, numFrames, startChan, numChans, transients, residual, order, blockSize, padSize, skew, threshFwd, threshBack, windowSize, clumpLength
		);
	}
}
