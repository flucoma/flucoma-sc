s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");
c = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/02-guit.wav");
d = Buffer.new(s);
)

(
// without basic params
Routine{
	t = Main.elapsedTime;
	FDCompose.process(s, srcBufNumA: b.bufnum, srcBufNumB: c.bufnum, dstBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

d.query;
d.play;
d.plot