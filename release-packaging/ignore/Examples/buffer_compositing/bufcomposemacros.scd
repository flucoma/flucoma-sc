// (re)set the source buffers
(
~low = Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(3,0) ++ 1)));
~mid =  Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(12,0) ++ 1)));
~high = Buffer.sendCollection(s, (Signal.sineFill(4410, Array.fill(48,0) ++ 1)));
~piano = Buffer.read(s,File.realpath(FluidBufCompose.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-SA-UprightPianoPedalWide.wav",0,4410);
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
				FluidBufCompose.process(s,x[i],destination:x[0], destStartFrame:x[0].numFrames);
			});
			"Done!".postln;
			}.play;
		}
	);
}
)
//test various combinations of concat
~concat.value([~low,~mid])
~concat.value([~mid,~low,~high])
~concat.value([~mid,~piano,~low])
~concat.value([~mid,~piano])

~mid.query