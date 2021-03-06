s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
c = Buffer.new(s);
d = Buffer.new(s);
e = Buffer.new(s);
)

(
// with basic params
Routine{
	t = Main.elapsedTime;
	FluidBufHPSS.process(s, b.bufnum, harmBufNum: c.bufnum, percBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

c.query;
c.play;
d.query;
d.play;
e.query;
e.play;

//nullsumming tests
{(PlayBuf.ar(1,c.bufnum))+(PlayBuf.ar(1,d.bufnum))+(-1*PlayBuf.ar(1,b.bufnum,doneAction:2))}.play


// with everything changed to make it much faster
(
Routine{
	t = Main.elapsedTime;
	FluidBufHPSS.process(s,b.bufnum, 44100, 44100, 0, 0, c.bufnum, d.bufnum, e.bufnum, 51, 31, 2); // need to change these for something sensible
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);


// owen's sexy example (The world's most expensive stereoizer)

(
{
	var hpss = FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1),modeFlag:2,hta1:SinOsc.kr(1.5,mul:20,add:20),hta2:SinOsc.kr(3,mul:25,add:25), pta1:SinOsc.kr(1.6,0,mul:30,add:30),pta2:SinOsc.kr(1.7,0,mul:17,add:24));
	[hpss[2]  + 0.5 * hpss[0], hpss[1] + 0.5 * hpss[0]];
}.play;
)