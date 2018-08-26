s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/denoise_stn/sources/01-mix.wav");
~fft_size = 2048;
~frame_size = 1024;
~hop_size = 256;
~which_rank = 0;
)

(
c = Buffer.alloc(s,b.numFrames,5);
x = Buffer.alloc(s,(~fft_size / 2 +1),5);
y = Buffer.alloc(s,(b.numFrames / ~hop_size + 1) ,5);
)

// without sources
d = Main.elapsedTime; b.fdNMF(nil, x, y, 5, 100, ~fft_size,~frame_size,~hop_size,{e = Main.elapsedTime; (e-d).postln})

// with sources
d = Main.elapsedTime; b.fdNMF(c, x, y, 5, 100, ~fft_size,~frame_size,~hop_size,{e = Main.elapsedTime; (e-d).postln})

//look at the dictionaries and activations
x.plot; y.plot;

//null test of the sum of sources
{(PlayBuf.ar(5,c.bufnum,doneAction:2).sum)+(-1*PlayBuf.ar(1,b.bufnum,doneAction:2))}.play

// play around
{Splay.ar(PlayBuf.ar(5,c.bufnum,doneAction:2))}.play

//play a single source
{PlayBuf.ar(5,c.bufnum,doneAction:2)[~which_rank].dup}.play

//play noise through a filter
(
{
	var chain;
	chain = FFT(LocalBuf(~fft_size), WhiteNoise.ar());

	chain = chain.pvcollect(~fft_size, {|mag, phase, index|
		[mag * BufRd.kr(5,x.bufnum,DC.kr(index),0,1)[~which_rank]];
	});

	IFFT(chain);
}.play
)

//play noise through an activation
{WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/~hop_size,0,(b.numFrames / ~hop_size + 1)),0,1)[~which_rank] * 0.01)}.play


//play noise through both activation and filter
(
{
	var chain;
	chain = FFT(LocalBuf(~fft_size), WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/~hop_size,0,(b.numFrames / ~hop_size + 1)),0,1)[~which_rank]*0.15),0.25,1);

	chain = chain.pvcollect(~fft_size, {|mag, phase, index|
		[mag * BufRd.kr(5,x.bufnum,DC.kr(index),0,1)[~which_rank]];
	});

	[0,IFFT(chain)];
}.play
)


// test with stereo input
Buffer.freeAll(s)

(
b = Buffer.read(s,"/Users/pa/Desktop/verystereo.wav");
~fft_size = 1024;
~frame_size = 512;
~hop_size = 256;
)

(
c = Buffer.alloc(s,b.numFrames,10);
x = Buffer.alloc(s,(~fft_size / 2 +1),10);
y = Buffer.alloc(s,(b.numFrames / ~hop_size + 1) ,10);
)

d = Main.elapsedTime; b.fdNMF(c, x, y, 5, 100, ~fft_size,~frame_size,~hop_size,{e = Main.elapsedTime; (e-d).postln})

{PlayBuf.ar(10,c.bufnum,doneAction:2)[5].dup}.play

c.getn(0, 10, {|x|x.postln})
