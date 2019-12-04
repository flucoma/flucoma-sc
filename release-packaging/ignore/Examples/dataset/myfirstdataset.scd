// TB2 SC Playground V0

/*
Current stinkers:
1) Producing flat datapoints for FluidDataSet (i.e. flattening and cherry picking a multichannel buffer) takes bloody ages due to all the server syncing. I can't work out how to do it reliably outwith a Routine (which would certainly be quicker, to my mind)
2) Functions from the classes don't yet directly return things, and you have to access their return data through actions. This is partly because I don't know what the correct thing to do w/r/t blocking is, so I'm hoping GR will do it properly
*/

//STEP 0: start server
s.reboot;

if(s.hasBooted.not){"Warning: server not running".postln};

//STEP 1: Get some files
(
Buffer.freeAll;
FileDialog.new(fileMode:2,okFunc:{|x| ~path = x[0];
	~audioBuffers = SoundFile.collectIntoBuffers(~path+/+'*',s);
	~lookup = Dictionary(n:~audioBuffers.size);
	~audioBuffers.do{|b| ~lookup.add(b.path->b)};
	});
)

//STEP 2: Make a FluidDataSet
~dataset = FluidDataSet.new(s,"mfccs", 96) //12 dims * 4 stats * 2 derivatives

//STEP 3A: EITHER populate the dataset like so (and cry about how long the data point assembly takes)
(
Routine{
    ~audioBuffers.do{|b|
        var tmpMFCCs = Buffer.new(s);
        var tmpStats = Buffer.new(s);
        var tmpFlat = Buffer.new(s,12 * 4 * 2, 1);
        s.sync;
        ("Analyzing" + b.path).postln;
        FluidBufMFCC.process(s,b,features: tmpMFCCs);
        FluidBufStats.process(s,source:tmpMFCCs, stats: tmpStats,numDerivs:1);
        "stats".postln;
         12.do{|i|
            //This takes ages becayse of server syncing :-(
            FluidBufCompose.process(s,tmpStats,0,2, i+1,1, destination: tmpFlat, destStartFrame: (i*8));
            FluidBufCompose.process(s,tmpStats,4,1, i+1,1, destination: tmpFlat, destStartFrame: (i*8) + 2);
            FluidBufCompose.process(s,tmpStats,6,1, i+1,1, destination:tmpFlat, destStartFrame: (i*8) + 3);
            FluidBufCompose.process(s,tmpStats,0,2, i+1,1, destination: tmpFlat, destStartFrame: (i*8) + 4);
            FluidBufCompose.process(s,tmpStats,4,1, i+1,1, destination: tmpFlat, destStartFrame: (i*8) + 6);
            FluidBufCompose.process(s,tmpStats,6,1, i+1,1, destination:tmpFlat, destStartFrame: (i*8) + 7);
          };
         ~dataset.addPoint(b.path,tmpFlat);
          tmpFlat.free;
          tmpStats.free;
          tmpMFCCs.free;
    };
}.play
)

//check
~dataset.size({|x| x.postln})

//save
(
FileDialog.new(fileMode: 0, acceptMode: 1, okFunc:{|x| var file = x[0];
	//if the file exists and is a json, delete it
	if ((file.splitext[1] == "json") && (File.existsCaseSensitive(file)), {File.delete(file);"File Overwritten".postln;});
	//if not json, make it so
	if (file.splitext[1] != "json", {file = file ++ ".json";});
	// then write
	~dataset.write(file);
});
)

//STEP 3B: OR load in one you rolled earlier
FileDialog.new(fileMode: 0, acceptMode: 0, okFunc:{|x| ~dataset.read(x[0])});

//peek
c = Buffer.new(s)
~dataset.getPoint(~audioBuffers[3].path,c, { c.getn(0,96,{|x| x.postln})})

/*************************************/
//FluidKDTree
~kdtree = FluidKDTree.new(s)
~kdtree.fit(~dataset,action:{"fit".postln})
//match
~kdtree.kNearest(c,5,{|x| ~matches = x;})
~kdtree.kNearestDist(c,5,{|x| x.postln})

~lookup[~matches[4]].postln

/*************************************/
//FluidKMeans
~kMeans= FluidKMeans.new(s)
~kMeans.fit(~dataset,k:5,action:{"fit".postln})
~kMeans.predictPoint(c,{|x|x.postln})
~labels = FluidLabelSet.new(s,"clusters")
~kMeans.predict(~dataset,~labels, {|x| x.postln})
~labels.getLabel(~audioBuffers[2].path,action:{|c| c.postln})
Routine{
    ~labels.size({|x|x.do {|i|
        forkIfNeeded{
            ~audioBuffers[i].path.postln;
            ~labels.getLabel(~audioBuffers[i].path,action:{|c| c.postln});
            s.sync;
        }
    }
    });
}.play
~labels.write(~path+/+"labels.json")