// define a few processes
(
~ds = FluidDataSet(s,\ds10);
~dsW = FluidDataSet(s,\ds10W);
~dsL = FluidDataSet(s,\ds10L);
//define as many buffers as we have parallel voices/threads in the extractor processing (default is 4)
~loudbuf = 4.collect{Buffer.new};
~weightbuf = 4.collect{Buffer.new};
~mfccbuf = 4.collect{Buffer.new};
~statsbuf = 4.collect{Buffer.new};
~flatbuf = 4.collect{Buffer.new};

// here we instantiate a loader as per example 0
~loader = FluidLoadFolder(File.realpath(FluidBufPitch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/");

// here we instantiate a further slicing step as per example 0
~slicer = FluidSliceCorpus({ |src,start,num,dest|
	FluidBufOnsetSlice.kr(src,start,num,metric: 9, minSliceLength: 17, indices:dest, threshold:0.2,blocking: 1)
});

// here we instantiate a process of description and dataset writing, as per example 0
~extractor = FluidProcessSlices({|src,start,num,data|
	var label, voice, mfcc, stats, flatten;
	label = data.key;
    voice = data.value[\voice];
	mfcc = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:1,blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice], stats:~statsbuf[voice], numDerivs: 1, trig:Done.kr(mfcc), blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	FluidDataSetWr.kr(~ds,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
});

// here we make another processor, this time with doing an amplitude weighing
~extractorW = FluidProcessSlices({|src,start,num,data|
	var label, voice, loud, weights, mfcc, stats, flatten;
	label = data.key;
    voice = data.value[\voice];
	mfcc = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:1,blocking: 1);
	loud = FluidBufLoudness.kr(src,startFrame:start,numFrames:num,numChans:1,features:~loudbuf[voice],trig:Done.kr(mfcc),blocking: 1);
	weights = FluidBufScale.kr(~loudbuf[voice],numChans: 1,destination: ~weightbuf[voice],inputLow: -70,inputHigh: 0, trig: Done.kr(loud),blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice], stats:~statsbuf[voice], numDerivs: 1, weights: ~weightbuf[voice], trig:Done.kr(weights), blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	FluidDataSetWr.kr(~dsW,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
});

// and here we make a little processor for loudness if we want to poke at it
~extractorL = FluidProcessSlices({|src,start,num,data|
	var label, voice, loud, stats, flatten;
	label = data.key;
    voice = data.value[\voice];
	loud = FluidBufLoudness.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:1,blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice], stats:~statsbuf[voice], numDerivs: 1, trig:Done.kr(loud), blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	FluidDataSetWr.kr(~dsL,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
});
)

//////////////////////////////////////////////////////////////////////////
//loading process

//load and play to test if it is that quick - it is!
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;{var start, stop; PlayBuf.ar(~loader.index[~loader.index.keys.asArray.last.asSymbol][\numchans],~loader.buffer,startPos: ~loader.index[~loader.index.keys.asArray.last.asSymbol][\bounds][0])}.play;});
)

//////////////////////////////////////////////////////////////////////////
// slicing process

// run the slicer
(
t = Main.elapsedTime;
~slicer.play(s,~loader.buffer,~loader.index,action:{(Main.elapsedTime - t).postln;"Slicing done".postln});
)

// or just pass in the loader
~slicer = ~loader;

//slice count
~slicer.index.keys.size

//////////////////////////////////////////////////////////////////////////
// description process

// run both descriptor extractor - here they are separate to the batch process duration
(
t = Main.elapsedTime;
~extractor.play(s,~loader.buffer,~slicer.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)

(
t = Main.elapsedTime;
~extractorW.play(s,~loader.buffer,~slicer.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)

//////////////////////////////////////////////////////////////////////////
// manipulating and querying the data

// extracting whatever stats we want. In this case, mean/std/lowest/highest, and the same on the first derivative - excluding MFCC0 as it is mostly volume, keeping MFCC1-12

~curated = FluidDataSet(s,\ds10c);
~curatedW = FluidDataSet(s,\ds10cW);

~curator = FluidDataSetQuery.new(s);
(
~curator.addRange(1,12,{
	~curator.addRange(14,12,{
		~curator.addRange(53,12,{
			~curator.addRange(79,12,{
				~curator.addRange(92,12,{
					~curator.addRange(105,12,{
						~curator.addRange(144,12,{
							~curator.addRange(170,12);
						});
					});
				});
			});
		});
	});
});
)
~curator.transform(~ds,~curated)
~curator.transform(~dsW,~curatedW)

//check the dimension count
~ds.print
~curated.print
~curatedW.print

//building a tree for each dataset
~tree = FluidKDTree(s,5);
~tree.fit(~ds,{"Fitted".postln;});
~treeW = FluidKDTree(s,5);
~treeW.fit(~dsW,{"Fitted".postln;});
~treeC = FluidKDTree(s,5);
~treeC.fit(~curated,{"Fitted".postln;});
~treeCW = FluidKDTree(s,5);
~treeCW.fit(~curatedW,{"Fitted".postln;});

//select a sound to match
// EITHER retrieve a random slice
~targetsound = Buffer(s);
~targetname = ~slicer.index.keys.asArray.scramble.last.asSymbol;
#a,b = ~slicer.index[~targetname][\bounds];
FluidBufCompose.process(s,~loader.buffer,a,(b-a),numChans: 1, destination: ~targetsound,action: {~targetsound.play;})

// OR just load a file in that buffer
~targetsound = Buffer.read(s,Platform.resourceDir +/+ "sounds/a11wlk01.wav");

//describe the sound to match
(
{
	var loud, weights, mfcc, stats, flatten, stats2, written;
	mfcc = FluidBufMFCC.kr(~targetsound,features:~mfccbuf[0],trig:1);
	stats = FluidBufStats.kr(~mfccbuf[0],stats:~statsbuf[0], numDerivs: 1,trig:Done.kr(mfcc));
	flatten = FluidBufFlatten.kr(~statsbuf[0],~flatbuf[0],trig:Done.kr(stats));
	loud = FluidBufLoudness.kr(~targetsound,features:~loudbuf[0],trig:Done.kr(flatten),blocking: 1);
	weights = FluidBufScale.kr(~loudbuf[0],numChans: 1,destination: ~weightbuf[0],inputLow: -70,inputHigh: 0,trig: Done.kr(loud),blocking: 1);
	stats2 = FluidBufStats.kr(~mfccbuf[0],stats:~statsbuf[0], numDerivs: 1, weights: ~weightbuf[0], trig:Done.kr(weights),blocking: 1);
	written = FluidBufFlatten.kr(~statsbuf[0],~flatbuf[1],trig:Done.kr(stats2));
	FreeSelf.kr(Done.kr(written));
}.play;
)

//go language side to extract the right dimensions
~flatbuf[0].getn(0,182,{|x|~curatedBuf = Buffer.loadCollection(s, x[[0,1,4,6,7,8,11,13].collect{|x|var y=x*13+1;(y..(y+11))}.flat].postln)})
~flatbuf[1].getn(0,182,{|x|~curatedWBuf = Buffer.loadCollection(s, x[[0,1,4,6,7,8,11,13].collect{|x|var y=x*13+1;(y..(y+11))}.flat].postln)})

//find its nearest neighbours
~tree.kNearest(~flatbuf[0],{|x| ~friends = x.postln;})
~treeW.kNearest(~flatbuf[1],{|x| ~friendsW = x.postln;})
~treeC.kNearest(~curatedBuf,{|x| ~friendsC = x.postln;})
~treeCW.kNearest(~curatedWBuf,{|x| ~friendsCW = x.postln;})


// play them in a row
(
Routine{
5.do{|i|
	var dur;
	v = ~slicer.index[~friends[i].asSymbol];
	dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
	{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2))}.play;
	~friends[i].postln;
	dur.wait;
	};
}.play;
)

(
Routine{
5.do{|i|
	var dur;
	v = ~slicer.index[~friendsW[i].asSymbol];
	dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
	{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2))}.play;
	~friendsW[i].postln;
	dur.wait;
	};
}.play;
)

(
Routine{
5.do{|i|
	var dur;
	v = ~slicer.index[~friendsC[i].asSymbol];
	dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
	{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2))}.play;
	~friendsC[i].postln;
	dur.wait;
	};
}.play;
)

(
Routine{
5.do{|i|
	var dur;
	v = ~slicer.index[~friendsCW[i].asSymbol];
	dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
	{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2))}.play;
	~friendsCW[i].postln;
	dur.wait;
	};
}.play;
)

//explore dynamic range (changing the weigting's value of 0 in lines 44 and 168 will change the various weights given to quieter parts of the signal
(
t = Main.elapsedTime;
~extractorL.play(s,~loader.buffer,~slicer.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)
~norm = FluidNormalize.new(s)
~norm.fit(~dsL)
~norm.dump({|x|x["data_min"][[8,12]].postln;x["data_max"][[8,12]].postln;})