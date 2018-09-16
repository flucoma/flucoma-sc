s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/fluid_decomposition/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav");
c = Buffer.new(s);
d = Buffer.new(s);
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

//nullsumming tests
{(PlayBuf.ar(1,c.bufnum))+(PlayBuf.ar(1,d.bufnum))+(-1*PlayBuf.ar(1,b.bufnum,doneAction:2))}.play


// with everything changed to make it much faster
(
Routine{
	t = Main.elapsedTime;
	FluidBufHPSS.process(s,b.bufnum, 44100, 44100, 0, 0, c.bufnum, d.bufnum, 51, 31  ,2048,512,4096); // need to change these for something sensible
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);


// owen's sexy example (The world's most expensive stereoizer)

(
{
	var hpss = FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1),harmonicBinaryMask:1,percussiveBinaryMask:1,  harmonicThreshAmp1:SinOsc.kr(1.5,mul:20,add:20),harmonicThreshAmp2:SinOsc.kr(3,mul:25,add:25), percussiveThreshAmp1:SinOsc.kr(1.6,0,mul:30,add:30),percussiveThreshAmp2:SinOsc.kr(1.7,0,mul:17,add:24));
	[hpss[2]  + 0.5 * hpss[0], hpss[1] + 0.5 * hpss[0]];
}.play;
)