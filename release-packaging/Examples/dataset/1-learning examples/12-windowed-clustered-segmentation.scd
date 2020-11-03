// load a source
b = Buffer.read(s,"/Volumes/machins/projets/newsfeed/sons/textes/Audio/synth/fromtexttospeech-AmE-George.wav")
b.play

//slightly oversegment with novelty
//segments should still make sense but might cut a few elements in 2 or 3
~originalslices = Buffer(s);
FluidBufNoveltySlice.process(s, b, indices: ~originalslices, feature: 1, kernelSize: 29, threshold: 0.05, filterSize: 5, hopSize: 128, action: {~originalslices.numFrames.postln;})

//test the segmentation by looping them
(
{
	BufRd.ar(1, b,
		Phasor.ar(0,1,
			BufRd.kr(1, ~originalslices,
				MouseX.kr(0, BufFrames.kr(~originalslices) - 1), 0, 1),
			BufRd.kr(1, ~originalslices,
				MouseX.kr(1, BufFrames.kr(~originalslices)), 0, 1),
			BufRd.kr(1,~originalslices,
				MouseX.kr(0, BufFrames.kr(~originalslices) - 1), 0, 1)), 0, 1);
}.play;
)

//analyse each segment with MFCCs in a dataset
~originalslices.getn(0,~originalslices.numFrames, {|x|~originalslicesarray = x; if ((x.last != b.numFrames), {~originalslicesarray = ~originalslicesarray ++ (b.numFrames)}); });//retrieve the indices and add the file boundary at the end if not there already

//iterates through the
//a few buffers and our dataset - with back and forth from the language
(
~mfccs = Buffer(s);
~stats = Buffer(s);
~flat = Buffer(s);
~slices = FluidDataSet(s,\slices);

Routine{
	s.sync;
	(~originalslicesarray.size - 1).do{|i|
		FluidBufMFCC.process(s, b, startFrame: ~originalslicesarray[i], numFrames: (~originalslicesarray[i+1] - ~originalslicesarray[i]), numChans: 1,features: ~mfccs, numCoeffs: 20, action: {
			FluidBufStats.process(s, ~mfccs, startChan: 1, stats: ~stats, action: {
				FluidBufFlatten.process(s, ~stats, ~flat, action: {
					~slices.addPoint(i.asSymbol, ~flat);
				});
			});
		});
	};
}.play;
)

~slices.print

//run a window over consecutive segments, forcing them in 2 classes, and merging the consecutive segments of similar class
//we overlap the analysis with the last (original) slice to check for continuity
(
~winSize = 4;//the number of consecutive items to split in 2 classes;
~query = FluidDataSetQuery(s);
~kmeans = FluidKMeans(s,2,100);
~windowDS = FluidDataSet(s,\windowDS);
~windowLS = FluidLabelSet(s,\windowLS);
~slices.dump{|x|~sliceDict = x;};
)

//the windowed function
(
~windowedFunct = {arg head, winSize;
	var nbass = [], assignments = [], tempDict = ();
	//check the size of everything to not overrun
	winSize = (~originalslicesarray.size - head).min(winSize);
	//copy the items to a subdataset from hear
	winSize.do{|i|
		tempDict.put((i.asString), ~sliceDict["data"][(i+head).asString]);//here one could curate which stats to take
		"whichslices:%\n".postf(i+head);
	};
	~windowDS.load(Dictionary.newFrom([\cols, 133, \data, tempDict]), action: {
		"% - loaded\n".postf(head);

		//kmeans 2 and retrieve ordered array of class assignations
		~kmeans.fitPredict(~windowDS, ~windowLS, action: {|x|
			nbass = x;
			"% - fitted1: ".postf(head); nbass.postln;

			if (nbass.includes(winSize.asFloat), {
				~kmeans.fitPredict(~windowDS, ~windowLS, {|x|
					nbass = x; "% - fitted2: ".postf(head); nbass.postln;
					if (nbass.includes(winSize.asFloat), {
						~kmeans.fitPredict(~windowDS, ~windowLS, {|x|
							nbass = x; "% - fitted3: ".postf(head); nbass.postln;
						});
					});
				});
			});

			~windowLS.dump{|x|
				var assignments = x.at("data").asSortedArray.flop[1].flatten;
				"% - assigned ".postf(head);

				assignments.postln;

				(winSize-1).do{|i|
					if (assignments[i+1] != assignments[i], {~indices= ~indices ++ (~originalslicesarray[head+i+1])});

				};
				//if we still have some frames to do, do them
				if (((head+winSize) < ~originalslicesarray.size), {
					"-----------------".postln;
					~windowedFunct.value(head + winSize, winSize);
				}, {~indices = ~indices ++ (b.numFrames); "done".postln;});//if we're done close the books
			};
		});
	});
}
)

//the job

~indices = [0];
~windowedFunct.value(0, 5);
~kmeans.numClusters = 3;

~indices.postln;

~windowDS.print

{var i = 8;BufRd.ar(1,b,Line.ar(~originalslicesarray[i],~originalslicesarray[i+1],(~originalslicesarray[i+1] - ~originalslicesarray[i])/b.sampleRate, doneAction: 2))}.play;
{var i = 4;BufRd.ar(1,b,Line.ar(~indices[i],~indices[i+1],(~indices[i+1] - ~indices[i])/b.sampleRate, doneAction: 2))}.play;

//export to reaper
(
//first create a new file that ends with rpp - it will overwrite if the file exists
f = File.new("/tmp/clusteredslices.rpp","w+");

if (f.isOpen , {
	//write the header
	f.write("<REAPER_PROJECT 0.1 \"5.99/OSX64\" 1603037150\n\n");

	//a first track with the originalslicearray
	//write the track header
	f.write("<TRACK\nNAME \"novelty output\"\n");
	// iterate through the items in the track
	(~originalslicesarray.size - 1).do{|i| f.write("<ITEM\nPOSITION " ++ (~originalslicesarray[i] / b.sampleRate) ++ "\nLENGTH " ++ ((~originalslicesarray[i+1] - ~originalslicesarray[i]) / b.sampleRate) ++ "\nNAME \"slice-" ++ i ++ "\"\nSOFFS " ++ (~originalslicesarray[i] / b.sampleRate) ++ "\n<SOURCE WAVE\nFILE \"" ++ b.path ++ "\"\n>\n>\n");};
	//write the track footer
	f.write(">\n");

	// a second track with the new ~indices
	//write the track header
	f.write("<TRACK\nNAME \"clustered output\"\n");
	// iterate through the items in the track
	(~indices.size - 1).do{|i| f.write("<ITEM\nPOSITION " ++ (~indices[i] / b.sampleRate) ++ "\nLENGTH " ++ ((~indices[i+1] - ~indices[i]) / b.sampleRate) ++ "\nNAME \"slice-" ++ i ++ "\"\nSOFFS " ++ (~indices[i] / b.sampleRate) ++ "\n<SOURCE WAVE\nFILE \"" ++ b.path ++ "\"\n>\n>\n");};
	//write the track footer
	f.write(">\n");

	//write the footer
	f.write(">\n");
	f.close;
});
)

