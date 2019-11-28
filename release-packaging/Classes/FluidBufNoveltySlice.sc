FluidBufNoveltySlice : UGen {
		*new1 { |rate, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0, blocking = 0 |

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;

		source.isNil.if {"FluidBufNoveltySlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufNoveltySlice:  Invalid features buffer".throw};

		^super.new1(rate, source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize, maxFFTSize, kernelSize, filterSize, doneAction, blocking);

	}

    *kr { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, doneAction = 0 |

		^this.multiNew(\control, source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize, doneAction);

	}

    *process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action |
		^FluidNRTProcess.new(
			server, this, action, [indices]
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize
		);
	}

    *processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, feature = 0, kernelSize = 3, threshold = 0.5, filterSize = 1, windowSize = 1024, hopSize = -1, fftSize = -1, action |
		^FluidNRTProcess.new(
			server, this, action, [indices], blocking:1
		).process(
			source, startFrame, numFrames, startChan, numChans, indices, feature, kernelSize, threshold, filterSize, windowSize, hopSize, fftSize
		);
	}
}
