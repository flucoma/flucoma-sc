// Lookup in a KDTree using melbands
// Demonstration of a massive parallel approach to batch process swiftly in SC

s.options.numBuffers = 16384 //The method below for doing the analysus quickly needs lots of buffers
s.reboot

//Step 0: Make a corpus

//We'll jam together some random flucoma sounds for illustrative purposes
//Get some files
(
~audioexamples_path = File.realpath(FluidBufMelBands.class.filenameSymbol).dirname.withTrailingSlash +/+ "../AudioFiles/*.wav";
~allTheSounds = SoundFile.collect(~audioexamples_path);
~testSounds = ~allTheSounds;
~testSounds.do{|f| f.path.postln}; // print out the files that are loaded
)

//Load the files into individual buffers:
(
~audio_buffers = ~testSounds.collect{|f|
	Buffer.readChannel(
		server: s,
		path:f.path,
		channels:[0],
		action:{("Loaded" + f.path).postln;}
	)
};
)

//Do a segmentation of each buffer, in parallel
(
fork{
	~index_buffers = ~audio_buffers.collect{Buffer.new};
	s.sync;
	~count = ~audio_buffers.size;
	~audio_buffers.do{|src,i|
		FluidBufOnsetSlice.process(
			server:s,
			source:src,
			indices:~index_buffers[i],
			metric: 9,
			threshold:0.2,
			minSliceLength: 17,
			action:{
				(~testSounds[i].path ++ ":" +  ~index_buffers[i].numFrames + "slices").postln;
				~count = ~count - 1;
				if(~count == 0){"Done slicing".postln};
			}
		);
	}
}
)

// we now have an array of index buffers, one per source buffer, each containing the segmentation points as a frame positions
// this allows us to make an array of sizes
~index_buffers.collect{|b| b.numFrames}.sum

//For each of these segments, let's make a datapoint using the mean melbands.
// There's a number of ways of skinning this cat w/r/t telling the server what to do, but here we want to minimize traffic between language and server, and also produce undertsandable code

//First, we'll grab the onset points as language-side arrays, then scroll through each slice getting the mean melbands
(
// - a dataset to keep the mean melbands in
~mels = FluidDataSet(s);
// - a dictionary to keep the slice points in for later playback
~slices = Dictionary();
//The code below (as well as needing lots of buffers), creates lots of threads and we need a big ass scheduling queue
~clock = TempoClock(queueSize:8192);
)


// Do the Mel analysis in a cunning parallel fashion
(
{
	var counter, remaining;
	var condition = Condition.new;  // used to create a test condition to pause the routine ...
	var index_arrays = Dictionary();

	"Process started. Please wait.".postln;

	~total_slice_count = ~index_buffers.collect{|b| b.numFrames}.sum + ~index_buffers.size; //we get an extra slice in buffer
	~featurebuffers = ~total_slice_count.collect{Buffer.new};  // create a buffer per slice

	//Make our dictionary FluidDataSet-shaped
	~slices.put("cols",3);//[bufnum,start,end] for each slice
	~slices.put("data",Dictionary());

	//Collect each set of onsets into a language side array and store them in a dict
	~index_buffers.do{|b,i| // iterate over the input buffer array
		{
			b.loadToFloatArray(  // load to language side array
				action:{|indices|
					//Glue the first and last samples of the buffer on to the index list, and place in dictionary with the
					//Buffer object as a key

					index_arrays.put(~audio_buffers[i], Array.newFrom([0] ++ indices ++ (~audio_buffers[i].numFrames - 1)));

					if(i==(~index_buffers.size-1)) {condition.unhang};
				}
			)
		}.fork(stackSize:~total_slice_count);
	};
	condition.hang; //Pause until all the callbacks above have completed
	"Arrays loaded. Starting on the analysis, please wait.".postln;

	//For each of these lists of points, we want to scroll over the indices in pairs and get some mel bands
	counter = 0;
	remaining = ~total_slice_count;

	s.sync;

	// now iterate over Dict and calc melbands

	index_arrays.keysValuesDo{|buffer, indices|
		indices.doAdjacentPairs{|start,end,num|
			var analysis = Routine({|counter|
				FluidBufMelBands.processBlocking(
					server:s,
					source:buffer,
					startFrame:start,
					numFrames:(end-1) - start,
					features:~featurebuffers[counter],
					action:{
						remaining = remaining - 1;
						if(remaining == 0) { ~numMelBands = ~featurebuffers[0].numChannels;condition.unhang };
					}
				);
			});

			~slices["data"].put(counter,[buffer.bufnum,start,end]);

			//I'm spawning new threads to wait for the analysis callback from the server. The final callback will un-hang this thread
			analysis.value(counter); //Done differently to other blocks because I need to pass in the value of counter
			counter = counter + 1;
		}
	};
	condition.hang;
	"Analysis of % slices done.\n".postf(~total_slice_count);
}.fork(clock:~clock);
)


// Run stats on each mel buffer

// create a stats buffer for each of the slices
~statsbuffers = ~total_slice_count.collect{Buffer.new}; // create n Slices buffers - to be filled with (40 mel bands * 7 stats)

// run stats on all the buffers
(
{
	var remaining = ~total_slice_count;
	~featurebuffers.do{|buffer,i|
		FluidBufStats.processBlocking(
			server:s,
			source:buffer,
			stats:~statsbuffers[i],
			action:{
				remaining = remaining - 1;
				if(remaining == 0) { "done".postln};
			}
		);
	};
}.fork(clock:~clock);
)

~featurebuffers.size

//Flatten each stats buffer into a data point
~flatbuffers = ~total_slice_count.collect{Buffer.new};// create an array of flatten stats

(
{
	var remaining = ~total_slice_count;
	~statsbuffers.do{|buffer,i|
		FluidBufFlatten.processBlocking(
			server:s,
			source:buffer,
			destination:~flatbuffers[i],
			action:{
				remaining = remaining - 1;
				if(remaining == 0) { "Got flat points".postln; };
			}
		);
	};
}.fork(clock:~clock);
)


//Ram each flat point into a data set. At this point we have more data than we need, but we'll prune in moment
(
"Filling dataset".postln;
~mels.clear;

// ~flatbuffers = flatbuffers;
~flatbuffers.do{|buf,i|
	~mels.addPoint(i,buf);
};

~mels.print;
)


// Prune & standardise

// Tidy up the temp arrays of buffers we do not need anymore

(
"Cleaning".postln;
(~featurebuffers ++ ~statsbuffers ++ ~flatbuffers).do{|buf| buf.free};
)

//Above we sneakily made a dictionary of slice data for playback (bufnum,start,end). Let's throw it in a dataset
~slicedata = FluidDataSet(s);  // will hold slice data (bufnum,start,end) for playback

//dict -> dataset
(
~slicedata.load(~slices);
~slicedata.print;
)

// Step 1. Let's prune and standardize before fitting to a tree
(
~meanmels = FluidDataSet(s);//will hold pruned mel data
~stdmels = FluidDataSet(s);//will standardised, pruned mel data
~standardizer = FluidStandardize(s);
~pruner = FluidDataSetQuery(s);
~tree = FluidKDTree(s,numNeighbours:10,lookupDataSet:~slicedata);//we have to supply the lookup data set when we make the tree (boo!)
)

//Prune, standardize and fit KDTree
(
{
	~meanmels.clear;
	~stdmels.clear;
	~pruner.addRange(0,~numMelBands).transform(~mels,~meanmels); //prune with a 'query' -- so this is dropping all but ~meanmels
	~standardizer.fitTransform(~meanmels,~stdmels);
	~tree.fit(~stdmels,{"KDTree ready".postln});
}.fork(clock:~clock);
)

~meanmels.print

//Step 2: Set the FluidStandardizer and FluidKDTree up for listening
//set the buffers and busses needed
(
~stdInputPoint = Buffer.alloc(s,40);
~stdOutputPoint = Buffer.alloc(s,40);
~treeOutputPoint = Buffer.alloc(s,3 * 10);//numNeighbours x triples of bufnum,start,end
)


// let's play a random sound (to make sure we understand our data structure!
(
{
	var randPoint, buf, start, stop, dur;

	randPoint = ~slices["data"].keys.asArray.scramble[0];	 //   this good way of getting - but recast as strong

	buf= ~slices["data"][randPoint][0];
	start = ~slices["data"][randPoint][1];
	stop = ~slices["data"][randPoint][2];

	dur = stop - start;

	BufRd.ar(1,buf, Line.ar(start,stop,dur/s.sampleRate, doneAction: 2), 0, 2);
}.play
)


// Query KD tree

// a target sound from outside our dataset
~inBuf = Buffer.readChannel(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav", numFrames:15000, channels:[0]);
~inBuf.play

//OR one from within (but just the begining so beware of the difference!)
~inBuf = Buffer.alloc(s,15000);
~randomSlice = ~slices["data"].keys.asArray.scramble[0];
~audio_buffers[~slices["data"][~randomSlice][0]].copyData(~inBuf,srcStartAt: ~slices["data"][~randomSlice][1], numSamples: 15000.min(~slices["data"][~randomSlice][2] - (~slices["data"][~randomSlice][1])));
~inBuf.play

// now try getting a point, playing it, grabbing nearest neighbour and playing it ...

(
~inBufMels = Buffer(s);
~inBufStats = Buffer(s);
~inBufFlat = Buffer(s);
~inBufComp = Buffer(s);
~inBufStand = Buffer(s);
)

// FluidBuf Compose is buf version of dataSetQuery

(
FluidBufMelBands.process(s, ~inBuf, features: ~inBufMels, action: {
	FluidBufStats.process(s, ~inBufMels, stats:~inBufStats, action: {
		FluidBufFlatten.process(s, ~inBufStats, destination:~inBufFlat, action: {
			FluidBufCompose.process(s, ~inBufFlat, numFrames: ~numMelBands, destination: ~inBufComp, action: {
				~standardizer.transformPoint(~inBufComp, ~inBufStand, {
					~tree.kNearest(~inBufStand,{ |a|a.postln;~nearest = a;})
				})
			})
		})
	})
})
)

// playback nearest in order
(
fork{
	~nearest.do{|i|
		var  buf, start, stop, dur;

		buf= ~slices["data"][i.asInteger][0];
		start = ~slices["data"][i.asInteger][1];
		stop = ~slices["data"][i.asInteger][2];
		dur = (stop - start)/ s.sampleRate;
		{BufRd.ar(1,buf, Line.ar(start,stop,dur, doneAction: 2), 0, 2);}.play;

		i.postln;
		dur.wait;
	};
}
)
