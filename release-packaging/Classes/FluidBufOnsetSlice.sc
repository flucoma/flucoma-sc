FluidBufOnsetSlice : FluidBufProcessor {

	*kr  { |source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, trig = 1, blocking = 0|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;
		metric = FluidOnsetSlice.prSelectMetric(metric) ?? {
			("FluidBufOnsetSlice: % is not a recognised metric")
			.format(metric).throw;
		};

		source.isNil.if {"FluidBufOnsetSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufOnsetSlice:  Invalid features buffer".throw};

		^FluidProxyUgen.kr(\FluidBufOnsetSliceTrigger, -1, source, startFrame, numFrames, startChan, numChans, indices, metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize, maxFFTSize, trig, blocking);
	}

	*process { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;
		metric = FluidOnsetSlice.prSelectMetric(metric);
		if (metric.isNil or: metric.isUGen) {
			("FluidBufOnsetSlice: % is not a recognised metric")
			.format(metric).throw;
		};

		source.isNil.if {"FluidBufOnsetSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufOnsetSlice:  Invalid features buffer".throw};

		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices,  metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize,maxFFTSize,0],freeWhenDone,action
		);
	}

	*processBlocking { |server, source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, indices, metric = 0, threshold = 0.5, minSliceLength = 2, filterSize = 5, frameDelta = 0, windowSize = 1024, hopSize = -1, fftSize = -1, freeWhenDone = true, action|

		var maxFFTSize = if (fftSize == -1) {windowSize.nextPowerOfTwo} {fftSize};

		source = source.asUGenInput;
		indices = indices.asUGenInput;
		metric = FluidOnsetSlice.prSelectMetric(metric);
		if (metric.isNil or: metric.isUGen) {
			("FluidBufOnsetSlice: % is not a recognised metric")
			.format(metric).throw;
		};

		source.isNil.if {"FluidBufOnsetSlice:  Invalid source buffer".throw};
		indices.isNil.if {"FluidBufOnsetSlice:  Invalid features buffer".throw};

		^this.new(
			server, nil, [indices]
		).processList(
			[source, startFrame, numFrames, startChan, numChans, indices,  metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize,maxFFTSize,1],freeWhenDone,action
		);
	}
}
FluidBufOnsetSliceTrigger : FluidProxyUgen {}
