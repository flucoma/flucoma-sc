FluidLoadFolder {
	var  path, idFunc,channelFunc;
	var < files;
	var < index;
	var < buffer;

	*new{ |path, idFunc, channelFunc |
		^super.newCopyArgs(path, idFunc,channelFunc);
	}

	play { |server, action|
		var sizes,channels,maxChan, startEnd,counter;
		server ?? {server = Server.default};
		files = SoundFile.collect(path +/+ '*');
		sizes = files.collect{|f|f.numFrames()};
		channels = files.collect{|f| f.numChannels()};
		startEnd = sizes.inject([0],{|a,b| a ++ (b + a[a.size - 1])}).slide(2).clump(2);
		maxChan = channels[channels.maxIndex];
		counter = 0;
		index = IdentityDictionary();
		forkIfNeeded{
			buffer = Buffer.alloc(server,sizes.reduce('+'),maxChan);
			server.sync;
			buffer.updateInfo;
			buffer.query;
			server.sync;
			this.files.do{|f,i|
				var channelMap,identifier,entry;
				OSCFunc({
					if(idFunc.notNil)
					{ identifier = idFunc.value(path,i) }
					{ identifier = (f.path.basename).asSymbol };
					entry = IdentityDictionary();
					entry.add(\bounds->startEnd[i]);
					entry.add(\numchans->f.numChannels);
					entry.add(\sr->f.sampleRate);
					entry.add(\path->f.path);
					index.add(identifier->entry);
					counter = counter + 1;
					if(counter == (files.size)) {action !? action.value(index)};
				},"/done",server.addr,argTemplate:["/b_readChannel"]).oneShot;
				if(channelFunc.notNil)
				{  channelMap = channelFunc.value(channels[i],maxChan,i) }
				{  channelMap = Array.series(channels[i]) ++ -1.dup(maxChan - channels[i])}; //using -1 as a silence channel ID to fill the blanks (see SC_BufReadCommand::CopyChannels)
				buffer.readChannel(f.path,bufStartFrame:startEnd[i][0], channels:channelMap);
			}
		};
	}
}


FluidSliceCorpus {
	var < sliceFunc, idFunc;
	var < index;

	*new { |sliceFunc, idFunc|
		^super.newCopyArgs(sliceFunc,idFunc);
	}

	play{ |server,sourceBuffer,bufIdx, action, tasks = 4|
		var counter, tmpIndices,perf,jobs,total,uid, completed, pointstotal;
		uid = UniqueID.next;
		sourceBuffer ?? {"No buffer to slice".error; ^nil};
		bufIdx ?? {"No slice point dictionary passed".error;^nil};
		server ?? {server = Server.default};
		index = IdentityDictionary();
		counter = 0;
		completed = 0;
		jobs = List.newFrom(bufIdx.keys);
		total = jobs.size;
		pointstotal = 0;
		perf = { |tmpIndices|
			var idx,v,k = jobs.pop;
			v = bufIdx[k];
			counter = counter + 1;
			idx = counter;
			("Slicing" + counter ++ "/" ++ total).postln;
			OSCFunc({
				tmpIndices.loadToFloatArray(action:{ |a|
					var sliceindex = 1;
					completed =  completed + 1;
					("FluidSliceCorpus:" + ( completed.asString ++ "/" ++ total)).postln;
					if(a[0] != -1){
						var rawPoints = Array.newFrom(a).asInteger;
						if(rawPoints[0] != [v[\bounds][0]]){rawPoints = [v[\bounds][0]] ++ rawPoints};
						if(rawPoints.last != [v[\bounds][1]]){rawPoints=rawPoints ++ [v[\bounds][1]]};

						rawPoints.doAdjacentPairs{|a,b|
							var dict;
							if ((b - a) >= 1){
								dict = IdentityDictionary();
								dict.putAll(v);
								dict[\bounds] = [a,b];
								index.add(((k ++ "-" ++sliceindex).asSymbol)->dict);
								sliceindex = sliceindex + 1;
							}
						}
					}{
						var dict = IdentityDictionary();
						dict.putAll(v);
						index.add((k ++ "-1").asSymbol->dict);
					};
					if(jobs.size > 0){perf.value(tmpIndices)}{ tmpIndices.free };
					if(completed == total) {action !? action.value(index)};
				})
			},'/doneslice' ++ uid ++ counter,server.addr).oneShot;
			{
				var numframes,onsets;
				numframes = v[\bounds].reverse.reduce('-');
				onsets = sliceFunc.value(sourceBuffer, v[\bounds][0],numframes,tmpIndices);
				SendReply.kr(Done.kr(onsets),'/doneslice' ++ uid ++ idx);
				FreeSelfWhenDone.kr(onsets);
			}.play;
		};
		tasks ?? {tasks  = 4};
		tasks.asInteger.min(jobs.size).do{perf.value(Buffer.new)};
	}
}

FluidProcessSlices{
	var < featureFunc;

	*new { |featureFunc|
		^super.newCopyArgs(featureFunc);
	}

	play{ |server, sourceBuffer, bufIdx, action, tasks = 4|
		var counter,perf,jobs,total,uid, completed;

		sourceBuffer ?? {"No buffer to slice".error; ^nil};
		bufIdx ?? {"No slice point dictionary passed".error;^nil};
		server ?? {server = Server.default};

		uid = UniqueID.next;
		jobs = List.newFrom(bufIdx.keys);
		total = jobs.size;
		counter = 0;
		completed = 0;
		perf = {|jobID|
			var idx,v, k = jobs.pop;
			v = bufIdx[k];
			counter = counter + 1;
			("Processing" + counter ++ "/" ++ total).postln;
			idx = counter;
			v[\index] = counter;
			v[\voice] = jobID;
			OSCFunc({
				completed = completed + 1;
				("FluidProcessSlices:" + (completed.asString ++ "/" ++ total)).postln;
				if(jobs.size > 0){perf.value(jobID)};
				if(completed == total){action !? action.value(v);};
			},"/doneFeature" ++ uid ++ counter,server.addr).oneShot;

			{
				var numframes,feature;
				numframes = v[\bounds].reverse.reduce('-');
				feature = featureFunc.value(sourceBuffer, v[\bounds][0], numframes, k->v);
				SendReply.kr(Done.kr(feature),'/doneFeature' ++ uid ++ idx);
				FreeSelfWhenDone.kr(feature);
			}.play(server);
		};
		tasks ?? {tasks  = 4};
		tasks.asInteger.min(jobs.size).do{|jobIDs|perf.value(jobIDs)};
	}
}
