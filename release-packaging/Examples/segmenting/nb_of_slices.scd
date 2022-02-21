(
b = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
c = Buffer.new(s);
)

(
// with basic params
Routine{
	var startTime, target, tolerance, startThresh, prevThresh, curThresh, curVal, prevVal, iters, maxIters, dVal, dThresh;

	startTime = Main.elapsedTime;
	prevThresh = 0.1; //initial threshold (between 0.00001 and 0.99999
	target = 10; //number of slices desired
	tolerance = 0; // the acceptable error in the number of slices yield
	maxIters = 100; //max number of iterations acceptable

	//makes a first iteration
	FluidBufNoveltySlice.process(s,b, indices: c, threshold:prevThresh).wait;
	prevVal = c.numFrames;

	//makes a second iteration
	if ( (prevVal <  target), {
		curThresh = (prevThresh * 0.5).max(0.000001);
	}, {
		curThresh = (prevThresh * 2).min(0.999999);
	});
	FluidBufNoveltySlice.process(s,b, indices: c, threshold:curThresh).wait;
	curVal = c.numFrames;

	//makes further iterations until the result is achieved, or the maximum of acceptable iterations is reached
	iters = 2;
	while ( {
		(iters < maxIters) && ((curVal - target).abs > tolerance)
	}, {
		iters = iters + 1;
		dVal = curVal - prevVal;
		dThresh = curThresh - prevThresh;

		prevThresh = curThresh;
		prevVal = curVal;

		if ( (dVal == 0), {
			//if we have not change results between the last 2 passes, make the variation of threshold bigger
			curThresh = (dThresh + curThresh).min(0.999999).max(0.000001);
		},{
			//if we have
			curThresh = (((dThresh / dVal) * (target - curVal)) + curThresh).min(0.999999).max(0.000001);
		});
		FluidBufNoveltySlice.process(s,b, indices: c, threshold:curThresh).wait;
		curVal = c.numFrames;
	}
	);
	//depending on the outcome, gives the right info back

	if ( iters >= maxIters, {
		// failed
		"Failed to find a suitable threshold in % seconds.\n".postf((Main.elapsedTime - startTime).round(0.01));
	}, {
		// succeeded
		"Found % as a suitable threshold for % slices in % seconds and % iterations.\n".postf(curThresh, curVal, (Main.elapsedTime - startTime).round(0.01), iters);
	}
	);
}.play
)
