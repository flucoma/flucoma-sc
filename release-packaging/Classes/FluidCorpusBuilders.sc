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
		files.postln;
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
					entry.add(\points->startEnd[i]);
					entry.add(\channels->f.numChannels);
					entry.add(\sampleRate->f.sampleRate);
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
		var counter, tmpIndices,perf,jobs,total,uid;
		uid = UniqueID.next;
		sourceBuffer ?? {"No buffer to slice".error; ^nil};
		bufIdx ?? {"No slice point dictionary passed".error;^nil};
		server ?? {server = Server.default};
		index = IdentityDictionary();
		counter = 0;
		jobs = List.newFrom(bufIdx.keys);
		total = jobs.size;
		tmpIndices = Buffer.new;
		perf = {
			var v,k = jobs.pop;
			v = bufIdx[k];
			OSCFunc({
				tmpIndices.loadToFloatArray(action:{ |a|
					counter = counter + 1;
					("FluidSliceCorpus:" + ( counter.asString ++ "/" ++ total)).postln;
					if(a[0] != -1){
						var slicePoints = Array.newFrom(a).slide(2).clump(2);
						slicePoints.do{|s,j|
							var label = (k ++ j).asSymbol;
							index.add(label->IdentityDictionary(proto:v));
							index.at(label).put(\points,s);
						}
					}{
						index.put((k++ '0').asSymbol->IdentityDictionary(proto:v));
					};
					if(jobs.size > 0){perf.value}
					{
						tmpIndices.free;
						action !? action.value(index);
					};
				})
			},'/doneslice' ++ uid ++ counter,server.addr).oneShot;

			{
				var numframes,onsets;
				numframes = v[\points].reverse.reduce('-');
				onsets = sliceFunc.value(sourceBuffer, v[\points][0],numframes,tmpIndices);
				SendReply.kr(Done.kr(onsets),'/doneslice' ++ uid ++ counter);
				FreeSelfWhenDone.kr(onsets);
			}.play;
		};
		perf.value;
	}
}

FluidProcessSlices{
	var < featureFunc, labelFunc;
	var < index;

	*new { |featureFunc, labelFunc|
		^super.newCopyArgs(featureFunc,labelFunc);
	}

	play{ |server,sourceBuffer,bufIdx, action|
		var counter, tmpIndices,perf,jobs,total,uid;

		sourceBuffer ?? {"No buffer to slice".error; ^nil};
		bufIdx ?? {"No slice point dictionary passed".error;^nil};
		server ?? {server = Server.default};
		index = IdentityDictionary();

		uid = UniqueID.next;
		jobs = List.newFrom(bufIdx.keys);
		total = jobs.size;
		counter = 0;

		perf = {
			var v, k = jobs.pop;
			v = bufIdx[k];
			OSCFunc({
				counter = counter + 1;
				("FluidProcessSlices:" + (counter.asString ++ "/" ++ total)).postln;
				if(jobs.size > 0){perf.value}
				{
					tmpIndices.free;
					action !? action.value(index);
				};
			},"/doneFeature" ++ uid ++ counter,server.addr).oneShot;

			{
				var numframes,feature;
				numframes = v[\points].reverse.reduce('-');
				feature = featureFunc.value(sourceBuffer, v[\points][0],numframes,counter);
				SendReply.kr(Done.kr(feature),'/doneFeature' ++ uid ++ counter);
				FreeSelfWhenDone.kr(feature);
			}.play(server);
		};
		perf.value;
	}
}
