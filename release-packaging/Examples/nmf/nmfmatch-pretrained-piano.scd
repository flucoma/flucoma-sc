// Using an 88-components piano base to do polyphonic pitch tracking

//load in the sound in and a pretrained basis
(
	b = Buffer.read(s,FluidFilesPath("Tremblay-SA-UprightPianoPedalWide.wav"));
	c = Buffer.read(s,FluidFilesPath("filters/piano-dicts.wav"));
)
b.play
c.query

//use the pretrained bases to compute activations of each notes to drive the amplitude of a resynth
(
{
	var source, resynth;
	source = PlayBuf.ar(2, b,loop:1).sum;
	resynth = SinOsc.ar((21..108).midicps, 0, FluidNMFMatch.kr(source,c,88,10,4096).madd(0.002)).sum;
	[source, resynth]
}.play
)


//now sample and hold the same stream to get notes identified, played and sent back via osc
(
{
	var source, resynth, chain, trig, acts;
	source = PlayBuf.ar(2,b,loop:1).sum;

	// built in attack detection, delayed until the stable part of the sound
	chain = FFT(LocalBuf(256), source);
	trig = TDelay.kr(Onsets.kr(chain, 0.5),0.1);

	// samples and holds activation values that are scaled and capped, in effect thresholding them
	acts = Latch.kr(FluidNMFMatch.kr(source,c,88,10,4096).linlin(15,20,0,0.1),trig);

	// resynths as in the previous example, with the values sent back to the language
	resynth = SinOsc.ar((21..108).midicps, 0, acts).sum;
	SendReply.kr(trig, '/activations', acts);
	[source, resynth]
	// [source, T2A.ar(trig)]
	// resynth
}.play
)

// define a receiver for the activations
(
	OSCdef(\listener, {|msg|
		var data = msg[3..];
		// removes the silent and spits out the indicies as midinote number
		data.collect({arg item, i; if (item > 0.01, {i + 21})}).reject({arg item; item.isNil}).postln;
	}, '/activations');
)
