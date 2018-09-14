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
	FluidBufSines.process(s,b.bufnum, 44100, 44100, 0, 0, c.bufnum, d.bufnum, 50,0.2,5,0.2,0.9,2048,512,4096); // need to change these for something sensible
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);
