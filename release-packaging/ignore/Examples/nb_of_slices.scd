(
b = Buffer.read(s,File.realpath(FluidBufNoveltySlice.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Nicol-LoopE-M.wav");
c = Buffer.new(s);
)

(
// with basic params
Routine{
	var startTime, goal, startThresh, prevThresh, curThresh, curVal, prevVal, iters, maxIters, dVal, dThresh;
	startTime = Main.elapsedTime;
	prevThresh = 0.1; //initial threshold (between 0.00001 and 0.99999
	goal = 10; //number of slices desired
	maxIters = 100; //max number of iterations acceptable

	//makes a first iteration
	FluidBufNoveltySlice.process(s,b, indices: c, threshold:prevThresh,action:{|x|prevVal = x.numFrames});
	s.sync;
	//makes a second iteration
	if ( (prevVal <  goal), {
		curThresh = (prevThresh * 0.5).max(0.000001);
	}, {
		curThresh = (prevThresh * 2).min(0.999999);
	});
	FluidBufNoveltySlice.process(s,b, indices: c, threshold:curThresh,action:{|x|curVal = x.numFrames});
	s.sync;

	//makes further iterations until the result is achieved, or the maximum of acceptable iterations is reached
	iters = 2;
	while ( {
		(iters < maxIters) && ((curVal - goal) != 0)
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
			curThresh = (((dThresh / dVal) * (goal - curVal)) + curThresh).min(0.999999).max(0.000001);
		});
		FluidBufNoveltySlice.process(s,b, indices: c, threshold:curThresh,action:{|x|curVal = x.numFrames});
		s.sync;
	}
	);
	//depending on the outcome, gives the right info back

	if ( iters >= maxIters, {
		// failed
		"Failed to find a suitable threshold in % seconds.\n".postf((Main.elapsedTime - startTime).round(0.01));
	}, {
		// succeeded
		"Found % as a suitable threshold in % seconds and % iterations.\n".postf(curThresh, (Main.elapsedTime - startTime).round(0.01), iters);
	}
	);
}.play
)
