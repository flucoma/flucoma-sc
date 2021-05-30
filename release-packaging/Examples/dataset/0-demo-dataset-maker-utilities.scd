// define a few processes
(
~ds = FluidDataSet(s);//no name needs to be provided
//define as many buffers as we have parallel voices/threads in the extractor processing (default is 4)
~mfccbuf = 4.collect{Buffer.new};
~statsbuf = 4.collect{Buffer.new};
~flatbuf = 4.collect{Buffer.new};

// here we instantiate a loader which creates a single large buffer with a dictionary of what was included in it
// ~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/sons/smallnum/");
~loader = FluidLoadFolder(File.realpath(FluidLoadFolder.class.filenameSymbol).dirname +/+ "../AudioFiles");

// here we instantiate a further slicing step if needs be, which iterate through all the items of the FluidLoadFolder and slice the slices with the declared function.
~slicer = FluidSliceCorpus({ |src,start,num,dest|
	FluidBufOnsetSlice.kr(src, start, num, metric: 9, minSliceLength: 17, indices:dest, threshold:0.7, blocking: 1)
});

// here we instantiate a process of description and dataset writing, which will run each slice of the previous slice and write the entry. Note the chain of Done.kr triggers.
~extractor = FluidProcessSlices({|src,start,num,data|
	var mfcc, stats, writer, flatten,mfccBuf, statsBuf, flatBuf, label, voice;
	label = data.key;
    voice = data.value[\voice];
	mfcc = FluidBufMFCC.kr(src, startFrame:start, numFrames:num, numChans:1, features:~mfccbuf[voice], trig:1, blocking: 1);
	stats = FluidBufStats.kr(~mfccbuf[voice], stats:~statsbuf[voice], trig:Done.kr(mfcc), blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice], destination:~flatbuf[voice], trig:Done.kr(stats), blocking: 1);
	writer = FluidDataSetWr.kr(~ds, label, nil, ~flatbuf[voice], trig: Done.kr(flatten), blocking: 1)
});
)


//////////////////////////////////////////////////////////////////////////
//loading process

// just run the loader
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;});
)

//load and play to test if it is that quick - it is!
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;{var start, stop; PlayBuf.ar(~loader.index[~loader.index.keys.asArray.last.asSymbol][\numchans],~loader.buffer,startPos: ~loader.index[~loader.index.keys.asArray.last.asSymbol][\bounds][0])}.play;});
)

//ref to the buffer
~loader.buffer
//size of item
~loader.index.keys.size
//a way to get all keys info sorted by time
~stuff = Array.newFrom(~loader.index.keys).sort.collect{|x|~loader.index[x][\bounds]}.sort{|a,b| a[0]<b[0]};

//or to iterate in the underlying dictionary (unsorted)
(
~loader.index.pairsDo{ |k,v,i|
	k.postln;
	v.pairsDo{|l,u,j|
		"\t\t\t".post;
		(l->u).postln;
	}
}
)

// or write to file a human readable, sorted version of the database after sorting it by index.
(
a = File(Platform.defaultTempDir ++ "sc-loading.json","w");
~stuffsorted = Array.newFrom(~loader.index.keys).sort{|a,b| ~loader.index[a][\bounds][0]< ~loader.index[b][\bounds][0]}.do{|k|
	v =  ~loader.index[k];
	a.write(k.asString ++  "\n");
	v.pairsDo{|l,u,j|
		a.write("\t\t\t" ++ (l->u).asString ++ "\n");
	}
};
a.close;
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

// iterate
(
~slicer.index.pairsDo{ |k,v,i|
	k.postln;
	v.pairsDo{|l,u,j|
		"\t\t\t".post;
		(l->u).postln;
	}
}
)

///// write to file in human readable format, in order.
(
a = File(Platform.defaultTempDir ++ "sc-spliting.json","w");
~stuffsorted = Array.newFrom(~slicer.index.keys).sort{|a,b| ~slicer.index[a][\bounds][0]< ~slicer.index[b][\bounds][0]}.do{|k|
	v =  ~slicer.index[k];
	a.write(k.asString ++  "\n");
	v.pairsDo{|l,u,j|
		a.write("\t\t\t" ++ (l->u).asString ++ "\n");
	}
};
a.close;
)

//////////////////////////////////////////////////////////////////////////
// description process

// just run the descriptor extractor
(
t = Main.elapsedTime;
~extractor.play(s,~loader.buffer,~slicer.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)

// write the dataset to file with the native JSON
~ds.write(Platform.defaultTempDir ++ "sc-dataset.json")

// open the file in your default json editor
(Platform.defaultTempDir ++ "sc-dataset.json").openOS

//////////////////////////////////////////////////////////////////////////
// manipulating and querying the data

//building a tree
~tree = FluidKDTree(s);
~tree.fit(~ds,{"Fitted".postln;});

//retrieve a sound to match
~targetsound = Buffer(s);
~targetname = ~slicer.index.keys.asArray.scramble[0].asSymbol;
#a,b = ~slicer.index[~targetname][\bounds];
FluidBufCompose.process(s,~loader.buffer,a,(b-a),numChans: 1, destination: ~targetsound,action: {~targetsound.play;})

//describe the sound to match
(
{
	var mfcc, stats, flatten;
	mfcc = FluidBufMFCC.kr(~targetsound,features:~mfccbuf[0],trig:1);
	stats = FluidBufStats.kr(~mfccbuf[0],stats:~statsbuf[0],trig:Done.kr(mfcc));
	flatten = FluidBufFlatten.kr(~statsbuf[0],destination:~flatbuf[0],trig:Done.kr(stats));
	FreeSelfWhenDone.kr(flatten);
}.play;
)

//find its nearest neighbours
~friends = Array;
~tree.numNeighbours = 5;
~tree.kNearest(~flatbuf[0],{|x| ~friends = x.postln;})

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
