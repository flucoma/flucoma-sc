// (re)set the source buffers
(
~low = Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(3,0) ++ 1)));
~mid =  Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(12,0) ++ 1)));
~high = Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(48,0) ++ 1)));
~piano = Buffer.read(s,FluidFilesPath("Tremblay-SA-UprightPianoPedalWide.wav"),0,8820);
)

// draw the buffers to see what happened
(
~low.plot;
~mid.plot;
~high.plot;
~piano.plot;
)

// define the concatenation macro
(
~concat = {
	arg x;
	if(x.class != Array,
		{
			"Error - Needs an array as argument".postln;
		}, {
			Routine{
			for (1,x.size - 1, {
				arg i;
				FluidBufCompose.process(s,x[i],destination:x[0], destStartFrame:x[0].numFrames).wait;
			});
			"Done!".postln;
			}.play;
		}
	);
}
)
// test various combinations of concatenation
~concat.value([~low,~mid])
~concat.value([~mid,~low,~high])
~concat.value([~mid,~piano,~low])
~concat.value([~mid,~piano])

// check the buffers for the results

////////////////////////////////

// define the merging macro
(
~merge = {
	arg x;
	if(x.class != Array,
		{
			"Error - Needs an array as argument".postln;
		}, {
			Routine{
			for (1,x.size - 1, {
				arg i;
				FluidBufCompose.process(s,x[i],destination:x[0],destGain:1);
			});
			"Done!".postln;
			}.play;
		}
	);
}
)
// test various combinations of merging
~merge.value([~low,~mid])
~merge.value([~mid,~low,~high])
~merge.value([~mid,~piano,~low])
~merge.value([~mid,~piano])

// check the buffers for the results

////////////////////////////////

// define the stacking macro
(
~stack = {
	arg x;
	if(x.class != Array,
		{
			"Error - Needs an array as argument".postln;
		}, {
			Routine{
			for (1,x.size - 1, {
				arg i;
				FluidBufCompose.process(s,x[i],destination:x[0], destStartChan:x[0].numChannels).wait;
			});
			"Done!".postln;
			}.play;
		}
	);
}
)
// test various combinations of stacking
~stack.value([~low,~mid])
~stack.value([~mid,~low,~high])
~stack.value([~mid,~piano,~low])
~stack.value([~mid,~piano])

// check the buffers for the results
