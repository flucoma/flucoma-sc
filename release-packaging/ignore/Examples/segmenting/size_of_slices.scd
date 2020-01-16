(
b = Buffer.read(s,File.realpath(FluidBufNoveltySlice.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Nicol-LoopE-M.wav");
c = Buffer.new(s);
)

(
// with basic params
Routine{
	var startTime, target, tolerance, startThresh, prevThresh, curThresh, curVal, prevVal, iters, maxIters, dVal, dThresh;
	startTime = Main.elapsedTime;
	prevThresh = 0.1; //initial threshold (between 0.00001 and 0.99999
	target = 0.3 * s.sampleRate; //average of slices desired
	tolerance = 0.01 * s.sampleRate; // the acceptable error in the number of slices yield
	maxIters = 100; //max number of iterations acceptable

	//makes a first iteration and checks the average of slight lenght
	FluidBufNoveltySlice.process(s,b, indices: c, threshold: prevThresh,action:{|x|x.getn(0,x.numFrames,{|y|var out; out = Array.new; y.doAdjacentPairs({|a,b|out = out ++ (b - a);});prevVal = out.mean;})});
	//makes a second iteration
	s.sync; // OWEN: why is this needed???
	"1: % % %\n".postf(prevThresh, prevVal, c.numFrames);
	if ( (prevVal >  target), {
		curThresh = (prevThresh * 0.5).max(0.000001);
	}, {
		curThresh = (prevThresh * 2).min(0.999999);
	});
	FluidBufNoveltySlice.process(s,b, indices: c, threshold: curThresh,action:{|x|x.getn(0,x.numFrames,{|y|var out; out = Array.new; y.doAdjacentPairs({|a,b|out = out ++ (b - a);});curVal = out.mean;})});
	s.sync;
	"2: % % %\n".postf(curThresh, curVal, c.numFrames);
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
		FluidBufNoveltySlice.process(s,b, indices: c, threshold: curThresh,action:{|x|x.getn(0,x.numFrames,{|y|var out; out = Array.new; y.doAdjacentPairs({|a,b|out = out ++ (b - a);});curVal = out.mean;})});
		s.sync;
		"%: % % %\n".postf(iters, curThresh, curVal, c.numFrames);
	}
	);
	//depending on the outcome, gives the right info back

	if ( iters >= maxIters, {
		// failed
		"Failed to find a suitable threshold in % seconds.\n".postf((Main.elapsedTime - startTime).round(0.01));
	}, {
		// succeeded
		"Found % as a suitable threshold for an average lenght of % samples per slices (% of target) in % seconds and % iterations.\n".postf(curThresh.round(0.001), curVal.asInt, (curVal/target).round(0.01), (Main.elapsedTime - startTime).round(0.01), iters);
	}
	);
}.play
)




//sanity check
c.getn(0,c.numFrames,{|x|a=x})

a
d = Array.new;
a.doAdjacentPairs({|a,b|d = d ++ (b - a);})
d.median
d.mean
{e=0;d.do({|x|e = e + (x - d.mean).squared});e = e / d.size; e = e.sqrt;}.value
f = d.sort
f.first
f.last
(f.blendAt((f.size-1) * 0.75) - f.blendAt((f.size-1) * 0.25))
