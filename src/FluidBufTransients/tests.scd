s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");
c = Buffer.new(s);
d = Buffer.new(s);
)

(
// without basic params
Routine{
	t = Main.elapsedTime;
	FDTransients.process(s,b.bufnum, transBufNum:c.bufnum, resBufNum:d.bufnum);
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
	FDTransients.process(s,b.bufnum, 44100, 44100, 0, 0, c.bufnum, d.bufnum, 100, 512,256,1,2,1,12,20);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

