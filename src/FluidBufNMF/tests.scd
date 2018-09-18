s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
c = Buffer.new(s);
x = Buffer.new(s);
y = Buffer.new(s);
~fft_size = 1024;
~frame_size = 512;
~hop_size = 256;
~which_rank = 0;
)

(
// without sources
Routine{
	t = Main.elapsedTime;
	FluidBufNMF.process(s,b.bufnum,0,-1,0,-1,nil,x.bufnum,0,y.bufnum,0,5,100,0,~frame_size,~hop_size,~fft_size);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
);

// with sources only
(
Routine{
	t = Main.elapsedTime;
	FluidBufNMF.process(s,b.bufnum, 0,-1,0,-1,c.bufnum,nil,0,nil,0,5,100,0,~frame_size,~hop_size,~fft_size);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
)

// with everything
(
Routine{
	t = Main.elapsedTime;
	FluidBufNMF.process(s,b.bufnum, 0,-1,0,-1,c.bufnum,x.bufnum,0,y.bufnum,0,5,100,0,~frame_size,~hop_size,~fft_size);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
)


//look at the dictionaries and activations
c.plot;x.plot; y.plot;
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
{WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/~hop_size,0,(b.numFrames / ~hop_size + 1)),0,1)[~which_rank])*0.5}.play

//play noise through both activation and filter
(
{
	var chain;
	chain = FFT(LocalBuf(~fft_size), WhiteNoise.ar(BufRd.kr(5,y.bufnum,Phasor.ar(1,1/~hop_size,0,(b.numFrames / ~hop_size + 1)),0,1)[~which_rank]*12),0.5,1);

	chain = chain.pvcollect(~fft_size, {|mag, phase, index|
		[mag * BufRd.kr(5,x.bufnum,DC.kr(index),0,1)[~which_rank]];
	});

	[0,IFFT(chain)];
}.play
)

// test with stereo input (dual mono for best segregation)
Buffer.freeAll(s)

(
Routine{
	b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
	c = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-AcousticStrums-M.wav".resolveRelative);
	d = Buffer.new(s);
	e = Buffer.new(s);
	x = Buffer.new(s);
	y = Buffer.new(s);
	~fft_size = 1024;
	~frame_size = 512;
	~hop_size = 256;
	s.sync;
	FluidBufCompose.process(s,b.bufnum,srcBufNumB:c.bufnum, srcGainB:0.6, dstStartChanB:1, dstBufNum:d.bufnum);
	s.sync;
	d.query;
}.play;
)

d.play

(
Routine{
	t = Main.elapsedTime;
	FluidBufNMF.process(s,d.bufnum,0,-1,0,-1,e.bufnum,x.bufnum,0,y.bufnum,0,5,100,0,~frame_size,~hop_size,~fft_size);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play;
)

e.query
x.query
y.query

//test a single rank
{PlayBuf.ar(10,e.bufnum,doneAction:2)[9].dup}.play

// play them all across in a sort of upmixed stereo...
{Splay.ar(PlayBuf.ar(10,e.bufnum,doneAction:2))}.play

//test process on a segment
(
Routine{
	t = Main.elapsedTime;
	FluidBufNMF.process(s,d.bufnum,44100,44100,0,1,c.bufnum,rank:2);
	s.sync;
	(Main.elapsedTime - t).postln;
	t = Main.elapsedTime;
	FluidBufNMF.process(s,d.bufnum,8810,44100,1,1,b.bufnum,rank:2);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play
)
c.query
c.play
b.query
b.play


// nmf on empty buffer
Buffer.freeAll;
(
Routine({
	b = Buffer.alloc(s,44100);
	c = Buffer.new(s);
	s.sync;
	FluidBufNMF.process(s,b.bufnum,dstBufNum:c.bufnum);
	s.sync;
	c.getn(0,100,{|x| x.postln});
}).play;
)

// fixed dictionaries experiment
Buffer.freeAll;

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-AcousticStrums-M.wav".resolveRelative);
c = Buffer.new(s);
x = Buffer.new(s);
e = Buffer.alloc(s,1,1);
y = Buffer.alloc(s,1,1);
)

(
Routine {
	FluidBufNMF.process(s,b.bufnum,0,88200,0,1, c.bufnum, x.bufnum, rank:10);
	s.sync;
	c.query;
}.play;
)

// test nullsum array
(0..9).do({|chan|FluidBufCompose.process(s,srcBufNumA: c.bufnum, startChanA:chan, nChansA: 1, srcBufNumB: y.bufnum, dstBufNum: y.bufnum)});
{(PlayBuf.ar(10,c.bufnum).sum)+(-1*PlayBuf.ar(1,y.bufnum))}.play

// find the picking
{PlayBuf.ar(10,c.bufnum)[2]}.play

// copy all the other ranks on itself (the 2 above is omited from the array and is in the 2nd compose
(
Routine{
	[ 0, 1, 3, 4, 5, 6, 7, 8, 9 ].do({|chan|FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA:chan, nChansA: 1, srcBufNumB: e.bufnum, dstBufNum: e.bufnum)});
	s.sync;
	e.query;
	s.sync;
	FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA: 2, nChansA: 1, srcBufNumB: e.bufnum, dstStartChanB: 1, dstBufNum: e.bufnum);
	s.sync;
	e.query;
}.play;
)

//process
(
Routine{
	FluidBufNMF.process(s, b.bufnum, dstBufNum: c.bufnum, dictBufNum: e.bufnum, dictFlag: 2, actBufNum:y.bufnum, rank:2);
	s.sync;
	c.query;
}.play;
)

c.play
e.plot
y.query
y.plot
