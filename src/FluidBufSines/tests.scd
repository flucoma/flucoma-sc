s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
c = Buffer.new(s);
d = Buffer.new(s);
)

(
// with basic params
Routine{
	t = Main.elapsedTime;
	FluidBufSines.process(s, b.bufnum, sineBufNum: c.bufnum, resBufNum:d.bufnum);
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
	FluidBufSines.process(s,b.bufnum, 44100, 88200, 0, 0, c.bufnum, d.bufnum, 30,0.3,3,0.1,0.9,2048,512,4096);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);
