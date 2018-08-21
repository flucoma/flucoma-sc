s.reboot
////////////////////////////
// test for efficiency

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/denoise_stn/sources/01-mix.wav");
b.play;

(
c = Buffer.alloc(s,b.numFrames,5);
x = Buffer.alloc(s,(1024 / 2 +1),5);
y = Buffer.alloc(s,(b.numFrames / 256 + 1) ,5);
)

d = Main.elapsedTime; b.fdNMF(c, x, y, 5, 100, 1024,1024,256,{e = Main.elapsedTime; (e-d).postln})

//look at the dictionaries and activations
x.plot; y.plot;

//null test of the sum of sources
{(PlayBuf.ar(5,c.bufnum,doneAction:2).sum)+(-1*PlayBuf.ar(1,b.bufnum,doneAction:2))}.play

// play around
{Splay.ar(PlayBuf.ar(5,c.bufnum,doneAction:2))}.play

//play a single source
{PlayBuf.ar(5,c.bufnum,doneAction:2)[0].dup}.play

//play noise through a filter
(
{
	var chain;
	chain = FFT(LocalBuf(1024), WhiteNoise.ar());

	chain = chain.pvcollect(1024, {|mag, phase, index|
		[mag * BufRd.kr(5,x.bufnum,DC.kr(index),0,1)[0]];
	});

	IFFT(chain);
}.play
)

//play noise through an activation
{WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/256,0,(b.numFrames / 256 + 1)),0,1)[0])}.play


//play noise through both activation and filter
(
{
	var chain;
	chain = FFT(LocalBuf(1024), WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/256,0,(b.numFrames / 256 + 1)),0,1)[0]),0.25);

	chain = chain.pvcollect(1024, {|mag, phase, index|
		[mag * BufRd.kr(5,x.bufnum,DC.kr(index),0,1)[0]];
	});

	IFFT(chain);
}.play
)
