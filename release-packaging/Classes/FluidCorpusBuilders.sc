FluidLoadFolder {
	var  path, labelFunc,channelFunc;
	var < files;
	var < index;
	var < buffer;

	*new{ |path, labelFunc, channelFunc |
		^super.newCopyArgs(path, labelFunc,channelFunc);
	}

	play { |server, action|
		var sizes,channels,maxChan, startEnd,counter;
		server ?? server = Server.default;
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
				var channelMap,label,entry;
				OSCFunc({
					if(labelFunc.notNil)
					{ label = labelFunc.value(path,i) }
					{ label = (f.path.basename).asSymbol };
					entry = IdentityDictionary();
					entry.add(\bounds->startEnd[i]);
					entry.add(\numchans->f.numChannels);
					entry.add(\sr->f.sampleRate);
					index.add(label->entry);
					counter = counter + 1;
					if(counter == (files.size)) {action !? action.value(index)};
				},"/done",server.addr,argTemplate:["/b_readChannel"]).oneShot;
				if(channelFunc.notNil)
				{  channelMap = channelFunc.value(channels[i],maxChan,i) }
				{  channelMap = Array.series(channels[i]).wrapExtend(maxChan) };
				buffer.readChannel(f.path,bufStartFrame:startEnd[i][0], channels:channelMap);
			}
		};
	}
}


FluidSliceCorpus {
	var < sliceFunc, labelFunc;
	var < index;

	*new { |sliceFunc, labelFunc|
		^super.newCopyArgs(sliceFunc,labelFunc);
	}

	play{ |server,sourceBuffer,bufIdx, action|
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
		4.do{perf.value(Buffer.new)};
	}
}

FluidProcessSlices{
	var < featureFunc, labelFunc;
	var < index;

	*new { |featureFunc, labelFunc|
		^super.newCopyArgs(featureFunc,labelFunc);
	}

	play{ |server,sourceBuffer,bufIdx, action|
		var counter,perf,jobs,total,uid, completed;

		sourceBuffer ?? {"No buffer to slice".error; ^nil};
		bufIdx ?? {"No slice point dictionary passed".error;^nil};
		server ?? {server = Server.default};
		index = IdentityDictionary();

		uid = UniqueID.next;
		jobs = List.newFrom(bufIdx.keys);
		total = jobs.size;
		counter = 0;
		completed = 0;
		perf = {
			var idx,v, k = jobs.pop;
			v = bufIdx[k];
			counter = counter + 1;
			idx = counter;
			OSCFunc({
				completed = completed + 1;
				("FluidProcessSlices:" + (completed.asString ++ "/" ++ total)).postln;
				if(jobs.size > 0){perf.value};
				if(completed == total){action !? action.value(index);};
			},"/doneFeature" ++ uid ++ counter,server.addr).oneShot;

			{
				var numframes,feature;
				numframes = v[\bounds].reverse.reduce('-');
				feature = featureFunc.value(sourceBuffer, v[\bounds][0],numframes,k);
				SendReply.kr(Done.kr(feature),'/doneFeature' ++ uid ++ idx);
				FreeSelfWhenDone.kr(feature);
			}.play(server);
		};
		4.do{perf.value};
	}
}
