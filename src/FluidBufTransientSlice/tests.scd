s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
c = Buffer.new(s);
)

(
// with basic params
Routine{
	t = Main.elapsedTime;
	FluidBufTransientSlice.process(s,b.bufnum, transBufNum:c.bufnum, order:80, debounce:4410);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

//check the number of slices
c.query;

//loops over a splice
(
{
	BufRd.ar(
		1,
		b.bufnum,
		Phasor.ar(
			0,
			1,
			BufRd.kr(
				1,
				c.bufnum,
				MouseX.kr(
					0,
					BufFrames.kr(c.bufnum) - 1),
				0,
				1),
			BufRd.kr(
				1,
				c.bufnum,
				MouseX.kr(
					1,
					BufFrames.kr(c.bufnum)),
				0,
				1),
			BufRd.kr(
				1,
				c.bufnum,
				MouseX.kr(
					0,
					BufFrames.kr(c.bufnum) - 1),
				0,
				1)),
		0,
		1)
}.play;
)


// with everything changed to make it much faster
(
Routine{
	t = Main.elapsedTime;
	FluidBufTransients.process(s,b.bufnum, 44100, 44100, 0, 0, c.bufnum, d.bufnum, 100, 512,256,1,2,1,12,20);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

