// load a source folder
~loader = FluidLoadFolder(File.realpath(FluidBufPitch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/");
~loader.play;

//slightly oversegment with novelty
//segments should still make sense but might cut a few elements in 2 or 3
~slicer = FluidSliceCorpus({ |src,start,num,dest| FluidBufNoveltySlice.kr(src,start,num,indices:dest, feature: 1, kernelSize: 29, threshold: 0.1, filterSize: 5, hopSize: 128)});
~slicer.play(s, ~loader.buffer,~loader.index);

//test the segmentation by looping them
(
~originalindices = Array.newFrom(~slicer.index.keys).sort{|a,b| ~slicer.index[a][\bounds][0]< ~slicer.index[b][\bounds][0]}.collect{|x|~slicer.index[x][\bounds]};
d = {arg start=0, end = 44100;
	BufRd.ar(1, ~loader.buffer, Phasor.ar(0,1,start,end,start),0,1);
}.play;

w = Window.new(bounds:Rect(100,100,400,60)).front;
b = ControlSpec(0, ~originalindices.size - 1, \linear, 1); // min, max, mapping, step
c = StaticText(w, Rect(340, 20, 50, 20)).align_(\center);
a = Slider(w, Rect(10, 20, 330, 20))
.action_({var val = b.map(a.value).asInteger;
	c.string_(val.asString);
	d.set(\start,~originalindices[val][0], \end, ~originalindices[val][1]);
});
)

//analyse each segment with 20 MFCCs in a dataset and spectralshapes in another one
(
~featuresbuf = 4.collect{Buffer.new};
~statsbuf = 4.collect{Buffer.new};
~flatbuf = 4.collect{Buffer.new};
~slicesMFCC = FluidDataSet(s);
~slicesShapes = FluidDataSet(s);
~extractor = FluidProcessSlices({|src,start,num,data|
	var features, stats, writer, flatten,mfccBuf, statsBuf, flatBuf, label, voice;
	label = data.key;
	voice = data.value[\voice];
	features = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1, numCoeffs: 20, features:~featuresbuf[voice],trig:1,blocking: 1);
	stats = FluidBufStats.kr(~featuresbuf[voice],stats:~statsbuf[voice],trig:Done.kr(features),blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	writer = FluidDataSetWr.kr(~slicesMFCC,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
	features = FluidBufSpectralShape.kr(src,startFrame:start,numFrames:num,numChans:1, features:~featuresbuf[voice],trig:Done.kr(writer),blocking: 1);
	stats = FluidBufStats.kr(~featuresbuf[voice],stats:~statsbuf[voice],trig:Done.kr(features),blocking: 1);
	flatten = FluidBufFlatten.kr(~statsbuf[voice],~flatbuf[voice],trig:Done.kr(stats),blocking: 1);
	writer = FluidDataSetWr.kr(~slicesShapes,label, -1, ~flatbuf[voice], Done.kr(flatten),blocking: 1);
});
)
~extractor.play(s,~loader.buffer, ~slicer.index);

~slicesMFCC.print
~slicesShapes.print

//run a window over consecutive segments, forcing them in 2 classes, and merging the consecutive segments of similar class
//we overlap the analysis with the last (original) slice to check for continuity
(
~winSize = 4;//the number of consecutive items to split in 2 classes;
~curated = FluidDataSet(s);
~query = FluidDataSetQuery(s);
~stan = FluidStandardize(s);
~kmeans = FluidKMeans(s,2,1000);
~windowDS = FluidDataSet(s);
~windowLS = FluidLabelSet(s);
)

//curate stats (MFCCs)
~query.clear
~query.addRange((0*20)+1,10);
~query.transform(~slicesMFCC,~curated);

//OR
//curate stats (moments)
~query.clear
~query.addRange(0,3);
~query.transform(~slicesShapes,~curated);

//OR
//curate both
~query.clear
~query.addColumn(0);//add col 0 (mean of mfcc0 as 'loudness')
~query.transform(~slicesMFCC,~curated);//mfcc0 as loudness
~query.clear;
~query.addRange(0,3);//add some spectral moments
~query.transformJoin(~slicesShapes, ~curated, ~curated);//join in centroids

//optionally standardize in place
~stan.fitTransform(~curated, ~curated);

~curated.print

//retrieve the dataset as dictionary
~curated.dump{|x|~sliceDict = x;};

~originalslicesarray = (~originalindices.flatten ++ ~loader.buffer.numFrames).asSet.asArray.sort
~orginalkeys = Array.newFrom(~slicer.index.keys).sort{|a,b| ~slicer.index[a][\bounds][0]< ~slicer.index[b][\bounds][0]}

//the windowed function, recursive to deal with sync dependencies
(
~windowedFunct = {arg head, winSize, overlap;
	var nbass = [], assignments = [], tempDict = ();
	//check the size of everything to not overrun
	winSize = (~originalslicesarray.size - head).min(winSize);
	//copy the items to a subdataset from hear
	winSize.do{|i|
		tempDict.put((i.asString), ~sliceDict["data"][(~orginalkeys[(i+head)]).asString]);//here one could curate which stats to take
		"whichslices:%\n".postf(i+head);
	};
	~windowDS.load(Dictionary.newFrom([\cols, ~sliceDict["cols"].asInteger, \data, tempDict]), action: {
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
					if (assignments[i+1] != assignments[i], {
						~newindices= ~newindices ++ (~originalslicesarray[head+i+1]).asInteger;
						~newkeys = ~newkeys ++ (~orginalkeys[head+i+1]);
					});

				};
				//if we still have some frames to do, do them
				if (((winSize + head) < ~originalslicesarray.size), {
					"-----------------".postln;
					~windowedFunct.value(head + winSize - overlap, winSize, overlap);
				}, {~newindices = (~newindices ++ ~loader.buffer.numFrames); "done".postln;});//if we're done close the books
			};
		});
	});
}
)

//the job

//test 1 - start at the begining, consider 4 items at a time, make 2 clusters, overlap 1
~newindices = [~originalslicesarray[0]]; ~newkeys = [~orginalkeys[0]];
~windowedFunct.value(0, 4, 1);

//OPTIONAL: try again with more clusters (3) and a wider window (6) and more overlap (2)
~newindices = [~originalslicesarray[0]]; ~newkeys = [~orginalkeys[0]];
~kmeans.numClusters = 3;
~windowedFunct.value(0,6,2);

//compare sizes
~orginalkeys.size
~newkeys.size;

//export to reaper
(
//first create a new file that ends with rpp - it will overwrite if the file exists
f = File.new("/tmp/clusteredslices-" ++ Date.getDate.stamp ++".rpp","w+");

if (f.isOpen , {
	var path, prevpath ="", sr, count, dur;
	//write the header
	f.write("<REAPER_PROJECT 0.1 \"5.99/OSX64\" 1603037150\n\n");

	//a first track with the originalslicearray
	//write the track header
	f.write("<TRACK\nNAME \"novelty output\"\n");
	// iterate through the items in the track
	~orginalkeys.do{|v, i|
		path = ~slicer.index[v][\path];
		if (path != prevpath, {
			sr = 	~slicer.index[v][\sr];
			prevpath = path;
			count = 0;
		});
		dur = ~originalslicesarray[i+1] - ~originalslicesarray[i];
		if ( dur > 0, {
			f.write("<ITEM\nPOSITION " ++ (~originalslicesarray[i] / sr) ++ "\nLENGTH " ++ (dur / sr) ++ "\nNAME \"" ++ v ++ "\"\nSOFFS " ++ (count / sr) ++ "\n<SOURCE WAVE\nFILE \"" ++ path ++ "\"\n>\n>\n");
		});
		count = count + dur;
	};
	//write the track footer
	f.write(">\n");

	// a second track with the new ~indices
	prevpath = "";
	//write the track header
	f.write("<TRACK\nNAME \"clustered output\"\n");
	// iterate through the items in the track
	~newkeys.do{|v, i|
		path = ~slicer.index[v][\path];
		if (path != prevpath, {
			sr = 	~slicer.index[v][\sr];
			prevpath = path;
			count = 0;
		});
		dur = ~newindices[i+1] - ~newindices[i];
		if (dur > 0, {
			f.write("<ITEM\nPOSITION " ++ (~newindices[i] / sr) ++ "\nLENGTH " ++ (dur / sr) ++ "\nNAME \"" ++ v ++ "\"\nSOFFS " ++ (count / sr) ++ "\n<SOURCE WAVE\nFILE \"" ++ path ++ "\"\n>\n>\n");
		});
		count = count + dur;
	};
	//write the track footer
	f.write(">\n");

	//write the footer
	f.write(">\n");
	f.close;
});
)

(then open the time-stamped reaper file clusterdslice in the folder tmp)