// define a few processes
(
~ds = FluidDataSet(s,\test);
~dsW = FluidDataSet(s,\testW);
//define as many buffers as we have parallel voices/threads in the extractor processing (default is 4)
~loudbuf = 4.collect{Buffer.new};
~mfccbuf = 4.collect{Buffer.new};
~statsbuf = 4.collect{Buffer.new};
~flatbuf = 4.collect{Buffer.new};

// here we instantiate a loader as per example 0
// ~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/sons/smallnum/");
~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/sons/segments/");

// here we instantiate a further slicing step as per example 0
~slicer = FluidSliceCorpus({ |src,start,num,dest|
	FluidBufOnsetSlice.kr(src,start,num,metric: 9, minSliceLength: 17, indices:dest, threshold:2,blocking: 1)
});

// here we instantiate a process of description and dataset writing, as per example 0
~extractor = FluidProcessSlices({|src,start,num,data|
	var label, voice, mfcc, stats, flatten;
	label = data.key;
    voice = data.value[\voice];
	mfcc = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:1,blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice],stats:~statsbuf[voice],trig:Done.kr(mfcc),blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	FluidDataSetWr.kr(~ds,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
});

// here we make another processor, this time with doing an amplitude weighing
~extractorW = FluidProcessSlices({|src,start,num,data|
	var label, voice, loud, loud2, loudbuf2, loud3, loudbuf3, mfcc, stats, flatten;
	loudbuf2 = LocalBuf.new((((num+1024) / 512) - 1).asInteger, 1); //temp buffer to extract what we need
	loudbuf3 = LocalBuf.new((((num+1024) / 512) - 1).asInteger, 1); //temp buffer to extract what we need
	label = data.key;
    voice = data.value[\voice];
	mfcc = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:1,blocking: 1);
	loud = FluidBufLoudness.kr(src,startFrame:start,numFrames:num,numChans:1,features:~loudbuf[voice],trig:Done.kr(mfcc),blocking: 1);
	loud2 = FluidBufCompose.kr(~loudbuf[voice],numChans: 1,destination: loudbuf2,trig: Done.kr(loud),blocking: 1);
	loud3 = FluidBufScale.kr(loudbuf2, loudbuf3,inlo: -110,inhi: 0,trig: Done.kr(loud2),blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice],stats:~statsbuf[voice], weights: loudbuf2, trig:Done.kr(loud3),blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	FluidDataSetWr.kr(~dsW,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
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

// just run the slicer
(
t = Main.elapsedTime;
~slicer.play(s,~loader.buffer,~loader.index,action:{(Main.elapsedTime - t).postln;"Slicing done".postln});
)

//slice count
~slicer.index.keys.size

//////////////////////////////////////////////////////////////////////////
// description process

// just run the descriptor extractor
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

//building a tree for each dataset
~tree = FluidKDTree(s);
~tree.fit(~ds,{"Fitted".postln;});
~treeW = FluidKDTree(s);
~treeW.fit(~dsW,{"Fitted".postln;});

//retrieve a sound to match
~targetsound = Buffer(s);
~targetname = ~slicer.index.keys.asArray.last.asSymbol;
#a,b = ~slicer.index[~targetname][\bounds];
FluidBufCompose.process(s,~loader.buffer,a,(b-a),numChans: 1, destination: ~targetsound,action: {~targetsound.play;})

//describe the sound to match
(
{
	var loud, loud2, loud3, mfcc, stats, flatten, stats2, loudbuf2, loudbuf3;
	loudbuf2 = LocalBuf.new((((~targetsound.numFrames+1024) / 512) - 1).asInteger, 1);
	loudbuf3 = LocalBuf.new((((~targetsound.numFrames+1024) / 512) - 1).asInteger, 1);
	mfcc = FluidBufMFCC.kr(~targetsound,features:~mfccbuf[0],trig:1);
	stats = FluidBufStats.kr(~mfccbuf[0],stats:~statsbuf[0],trig:Done.kr(mfcc));
	flatten = FluidBufFlatten.kr(~statsbuf[0],~flatbuf[0],trig:Done.kr(stats));
	loud = FluidBufLoudness.kr(~targetsound,features:~loudbuf[0],trig:Done.kr(flatten),blocking: 1);
	loud2 = FluidBufCompose.kr(~loudbuf[0],numChans: 1,destination: loudbuf2,trig: Done.kr(loud),blocking: 1);
	loud3 = FluidBufScale.kr(loudbuf2, loudbuf3,inlo: -110,inhi: 0,trig: Done.kr(loud2),blocking: 1);
	stats2 = FluidBufStats.kr(~mfccbuf[0],stats:~statsbuf[0], weights: loudbuf2, trig:Done.kr(loud3),blocking: 1);
	FluidBufFlatten.kr(~statsbuf[0],~flatbuf[1],trig:Done.kr(stats2));
}.play;
)

//find its nearest neighbours
~friends = Array;
~tree.numNeighbours = 5;
~tree.kNearest(~flatbuf[0],{|x| ~friends = x.postln;})
~friendsW = Array;
~treeW.numNeighbours = 5;
~treeW.kNearest(~flatbuf[1],{|x| ~friendsW = x.postln;})

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