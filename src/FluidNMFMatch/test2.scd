//designing the guitar example
(
{
	var source, todelay, delay1, delay2, delay3, feedback, mod1, mod2, mod3, mod4;
	//read the source
	source = PlayBuf.ar(1, b.bufnum);

	// generate modulators that are coprime in frequency
	mod1 = SinOsc.ar(1, 0, 0.001);
	mod2 = SinOsc.ar(((617 * 181) / (461 * 991)), 0, 0.001);
	mod3 = SinOsc.ar(((607 * 193) / (491 * 701)), 0, 0.001);
	mod4 = SinOsc.ar(((613 * 191) / (463 * 601)), 0, 0.001);

	// compress the signal to send to the delays
	todelay = DelayN.ar(source,0.1, 800/44100, //delaying it to compensate for FluidNMFMatch's latency
		LagUD.ar(K2A.ar(FluidNMFMatch.kr(source,e.bufnum,2,fftSize:2048)[0]), //reading the channel of the activations on the pick dictionary
			80/44100, // lag uptime (compressor's attack)
			1000/44100, // lag downtime (compressor's decay)
			(1/(2.dbamp) // compressor's threshold inverted
	)).clip(1,1000).pow((8.reciprocal)-1)); //clipping it so we only affect above threshold, then ratio(8) becomes the exponent of that base

	// delay network
	feedback = LocalIn.ar(3);// take the feedback in for the delays
	delay1 = DelayC.ar(BPF.ar(todelay+feedback[1]+(feedback[2] * 0.3), 987, 6.7,0.8),0.123,0.122+(mod1*mod2));
	delay2 = DelayC.ar(BPF.ar(todelay+feedback[0]+(feedback[2] * 0.3), 1987, 6.7,0.8),0.345,0.344+(mod3*mod4));
	delay3 = DelayC.ar(BPF.ar(todelay+feedback[1], 1456, 6.7,0.8),0.567,0.566+(mod1*mod3),0.6);
	LocalOut.ar([delay1,delay2, delay3]); // write the feedback for the delays

	//listen to the delays only by uncommenting the following line
	// [delay1+delay3,delay2+delay3]
	source.dup + ([delay1+delay3,delay2+delay3]*(-3.dbamp))
}.play;
)


//doing the piano training
//load in the sound in and a pretrained dictionary
(
b = Buffer.read(s,File.realpath(FluidNMFMatch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-SA-UprightPianoPedalWide.wav");
c = Buffer.read(s,File.realpath(FluidNMFMatch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/filters/piano-dicts.wav");
)
b.play
c.query

//bufcompose a 88 note version
// d = Buffer.alloc(s,1);
// FluidBufCompose.process(s,c.bufnum,nChansA:88,srcBufNumB:d.bufnum,dstBufNum:d.bufnum)
// d.query
// d.write("/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/fluid_decomposition/AudioFiles/filters/piano-dicts.wav", "wav", "float32")

//now use the activations of each notes to generate a resynth
(
x = { arg dabuf = 1;
	var source, resynth;
	source = PlayBuf.ar(2, b.bufnum,loop:1).sum;
	resynth = SinOsc.ar((21..108).midicps, 0, FluidNMFMatch.kr(source,dabuf,88,10,4096).madd(0.002)).sum;
	[source, resynth]
}.play
)

d = Buffer.read(s,"/Users/pa/Desktop/piano-dicts/seed-dc-01.wav");

x.set(\dabuf, d.bufnum)

//now sample and hold the same stream to get notes identified, played and sent back via osc
(
{
	var source, resynth, chain, trig, acts;
	source = PlayBuf.ar(2,b.bufnum,loop:1).sum;

	// built in attack detection, delayed until the stable part of the sound
	chain = FFT(LocalBuf(256), source);
	trig = TDelay.kr(Onsets.kr(chain, 0.5),0.1);

	// samples and holds activation values that are scaled and capped, in effect thresholding them
	acts = Latch.kr(FluidNMFMatch.kr(source,c.bufnum,88,10,4096).linlin(15,20,0,0.1),trig);

	// resynths as in the previous example, with the values sent back to the language
	resynth = SinOsc.ar((21..108).midicps, 0, acts).sum;
	SendReply.kr(trig, '/activations', acts);
	[source, resynth]
	// [source, T2A.ar(trig)]
	// resynth
}.play
)

// define a receiver for the activations
(
OSCdef(\listener, {|msg|
	var data = msg[3..];
	// removes the silent and spits out the indicies as midinote number
	data.collect({arg item, i; if (item > 0.01, {i + 21})}).reject({arg item; item.isNil}).postln;
}, '/activations');
)



// (0..10).collect({arg item, i; if (item > 5, {i})}).reject({arg item; item.isNil}).postln;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//set some buffers
(
b = Buffer.read(s,File.realpath(FluidNMFMatch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-BaB-SoundscapeGolcarWithDog.wav");
c = Buffer.new(s);
x = Buffer.new(s);
e = Buffer.alloc(s,1,1);
)

// train where all objects are present
(
Routine {
    FluidBufNMF.process(s,b.bufnum,130000,150000,0,1, c.bufnum, x.bufnum, rank:10);
    s.sync;
    c.query;
}.play;
)

// wait for the query to print
// then find a rank for each item you want to find. You could also sum them. Try to find a rank with a good object-to-rest ratio
(
    ~dog =0;
    {PlayBuf.ar(10,c.bufnum)[~dog]}.play
)

(
    ~bird = 5;
    {PlayBuf.ar(10,c.bufnum)[~bird]}.play
)


// copy at least one other rank to a third rank, a sort of left-over channel
(
Routine{
	FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA:~dog, nChansA: 1, srcBufNumB: e.bufnum, dstBufNum: e.bufnum);
	FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA:~bird, nChansA: 1, dstStartChanA: 1, srcBufNumB: e.bufnum, dstBufNum: e.bufnum);
    s.sync;
	(0..9).removeAll([~dog,~bird]).do({|chan|FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA:chan, nChansA: 1, dstStartChanA: 2, srcBufNumB: e.bufnum, dstBufNum: e.bufnum)});
    s.sync;
    e.query;
}.play;
)
e.plot;

//using this trained dictionary we can then see the activation...
(
{
	var source, blips;
	//read the source
	source = PlayBuf.ar(2, b.bufnum);
	blips = FluidNMFMatch.kr(source.sum,e.bufnum,3);
	}.plot(10);
)

// ...and use some threshold to 'find' objects...
(
{
	var source, blips;
	//read the source
	source = PlayBuf.ar(2, b.bufnum);
	blips = Schmidt.kr(FluidNMFMatch.kr(source.sum,e.bufnum,3),0.5,[10,1,1000]);
	}.plot(10);
)

// ...and use these to sonify them
(
{
	var source, blips, dogs, birds;
	//read the source
	source = PlayBuf.ar(2, b.bufnum);
	blips = Schmidt.kr(FluidNMFMatch.kr(source.sum,e.bufnum,3),0.5,[10,1,1000]);
	dogs = SinOsc.ar(100,0,Lag.kr(blips[0],0.05,0.15));
	birds = SinOsc.ar(1000,0,Lag.kr(blips[1],0.05,0.05));
	[dogs, birds] + source;
	}.play;
)
