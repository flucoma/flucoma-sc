
// define a few processes
(
~globalDS = FluidDataSet(s,\global11);
~pitchDS = FluidDataSet(s,\pitch11);
~loudDS = FluidDataSet(s,\loud11);
~mfccDS = FluidDataSet(s,\mfcc11);
~timbreDS = FluidDataSet(s,\timbre11);

//define as many buffers as we have parallel voices/threads in the extractor processing (default is 4)
~pitchbuf = 4.collect{Buffer.new};
~statsPitchbuf = 4.collect{Buffer.new};
~weightPitchbuf = 4.collect{Buffer.new};
~flatPitchbuf = 4.collect{Buffer.new};
~loudbuf = 4.collect{Buffer.new};
~statsLoudbuf = 4.collect{Buffer.new};
~flatLoudbuf = 4.collect{Buffer.new};
~weightMFCCbuf = 4.collect{Buffer.new};
~mfccbuf = 4.collect{Buffer.new};
~statsMFCCbuf = 4.collect{Buffer.new};
~flatMFCCbuf = 4.collect{Buffer.new};

// here we instantiate a loader as per example 0
// ~loader = FluidLoadFolder(File.realpath(FluidBufPitch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/");
~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/sons/smallnum/");
// ~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/sons/segments/");

// here we instantiate a further slicing step as per example 0
~slicer = FluidSliceCorpus({ |src,start,num,dest|
	FluidBufOnsetSlice.kr(src,start,num,metric: 9, minSliceLength: 17, indices:dest, threshold:0.2,blocking: 1)
});


// here we make the full processor building our 3 source datasets
~extractor = FluidProcessSlices({|src,start,num,data|
	var label, voice, pitch, pitchweights, pitchstats, pitchflat, loud, statsLoud, flattenLoud, mfcc, mfccweights, mfccstats, mfccflat, writePitch, writeLoud;
	label = data.key;
    voice = data.value[\voice];
	// the pitch computation is independant so it starts right away
	pitch = FluidBufPitch.kr(src, startFrame:start, numFrames:num, numChans:1, features:~pitchbuf[voice], unit: 1, trig:1, blocking: 1);
	// pitchweights = FluidBufThresh.kr(~pitchbuf[voice], numChans: 1, startChan: 1, destination: ~weightPitchbuf[voice], threshold: 0.1, trig:Done.kr(pitch), blocking: 1);//pull down low conf
	pitchweights = FluidBufScale.kr(~pitchbuf[voice], numChans: 1, startChan: 1, destination: ~weightPitchbuf[voice],inputLow: -1, trig:Done.kr(pitch), blocking: 1);
	pitchstats = FluidBufStats.kr(~pitchbuf[voice], stats:~statsPitchbuf[voice], numDerivs: 1, weights: ~weightPitchbuf[voice], outliersCutoff: 1.5, trig:Done.kr(pitchweights), blocking: 1);
	pitchflat = FluidBufFlatten.kr(~statsPitchbuf[voice],~flatPitchbuf[voice],trig:Done.kr(pitchstats),blocking: 1);
	writePitch = FluidDataSetWr.kr(~pitchDS,label, -1, ~flatPitchbuf[voice], Done.kr(pitchflat),blocking: 1);
	// the mfcc need loudness to weigh, so let's start with that
	loud = FluidBufLoudness.kr(src,startFrame:start, numFrames:num, numChans:1, features:~loudbuf[voice], trig:Done.kr(writePitch), blocking: 1);//here trig was 1
	//we can now flatten and write Loudness in its own trigger tree
	statsLoud = FluidBufStats.kr(~loudbuf[voice], stats:~statsLoudbuf[voice], numDerivs: 1, trig:Done.kr(loud), blocking: 1);
	flattenLoud = FluidBufFlatten.kr(~statsLoudbuf[voice],~flatLoudbuf[voice],trig:Done.kr(statsLoud),blocking: 1);
	writeLoud = FluidDataSetWr.kr(~loudDS,label, -1, ~flatLoudbuf[voice], Done.kr(flattenLoud),blocking: 1);
	//we can resume from the loud computation trigger
	mfcc = FluidBufMFCC.kr(src,startFrame:start,numFrames:num,numChans:1,features:~mfccbuf[voice],trig:Done.kr(writeLoud),blocking: 1);//here trig was loud
	mfccweights = FluidBufScale.kr(~loudbuf[voice],numChans: 1,destination: ~weightMFCCbuf[voice],inputLow: -70,inputHigh: 0, trig: Done.kr(mfcc), blocking: 1);
	mfccstats = FluidBufStats.kr(~mfccbuf[voice], stats:~statsMFCCbuf[voice], startChan: 1, numDerivs: 1, weights: ~weightMFCCbuf[voice], trig:Done.kr(mfccweights), blocking: 1);//remove mfcc0 and weigh by loudness instead
	mfccflat = FluidBufFlatten.kr(~statsMFCCbuf[voice],~flatMFCCbuf[voice],trig:Done.kr(mfccstats),blocking: 1);
	FluidDataSetWr.kr(~mfccDS,label, -1, ~flatMFCCbuf[voice], Done.kr(mfccflat),blocking: 1);
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
//slice count
~slicer.index.keys.size

//////////////////////////////////////////////////////////////////////////
// description process

// run the descriptor extractor
(
t = Main.elapsedTime;
~extractor.play(s,~loader.buffer,~slicer.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)

//////////////////////////////////////////////////////////////////////////
// manipulating and querying the data

~pitchDS.print;
~loudDS.print;
~mfccDS.print;

~loudbuf[0].query
~statsLoudbuf[0].query
~flatLoudbuf[0].query

~pitchbuf[0].query
~statsPitchbuf[0].query
~weightPitchbuf[0].query
~flatPitchbuf[0].query

//standardise
//AE ou PCA

//normalize

//query

//segquerymusaik
