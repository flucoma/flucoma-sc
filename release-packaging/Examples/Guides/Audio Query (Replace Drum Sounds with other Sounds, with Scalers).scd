/*
=================================================
|                                               |
|       LOAD AND ANALYZE THE SOURCE MATERIAL    |
|                                               |
=================================================
*/

(
// ============= 1. LOAD SOME FILES TO BE THE SOURCE MATERIAL ===================
// put your own folder path here! it's best if they're all mono for now.
~source_files_folder = FluidFilesPath();

~loader = FluidLoadFolder(~source_files_folder); // this is a nice helper class that will load a bunch of files from a folder.
~loader.play(s,{ // .play will cause it to *actually* do the loading

	// we really just want access to the buffer. there is also a .index with some info about the files
	// but we'll igore that for now
	~source_buf = ~loader.buffer;

	"all files loaded".postln;

	// double check if they're all mono? the buffer of the loaded files will have as many channels as the file with the most channels
	// so if this is 1, then we know all the files were mono.
	"num channels: %".format(~source_buf.numChannels).postln
});
)

(
// ==================== 2. SLICE THE SOURCE MATERIAL ACCORDING TO SPECTRAL ONSETS =========================
~source_indices_buf = Buffer(s); // a buffer for writing the indices into
FluidBufOnsetSlice.process(s,~source_buf,indices:~source_indices_buf,metric:9,threshold:0.5,minSliceLength:9,action:{ // do the slicing
	~source_indices_buf.loadToFloatArray(action:{
		arg indices_array;

		// post the results so that you can tweak the parameters and get what you want
		"found % slices".format(indices_array.size-1).postln;
		"average length: % seconds".format((~source_buf.duration / (indices_array.size-1)).round(0.001)).postln;
	})
});
)

(
// =========================== 3. DEFINE A FUNCTION FOR DOING THE ANALYSIS ===================================
~analyze_to_dataset = {
	arg audio_buffer, slices_buffer, action; // the audio buffer to analyze, a buffer with the slice points, and an action to execute when done
	~nmfccs = 13;
	Routine{
		var features_buf = Buffer(s); // a buffer for writing the MFCC analyses into
		var stats_buf = Buffer(s);  // a buffer for writing the statistical summary of the MFCC analyses into
		var flat_buf = Buffer(s); // a buffer for writing only he mean MFCC values into
		var dataset = FluidDataSet(s); // the dataset that all of these analyses will be stored in
		slices_buffer.loadToFloatArray(action:{ // get the indices from the server loaded down to the language
			arg slices_array;

			// iterate over each index in this array, paired with this next neighbor so that we know where to start
			// and stop the analysis
			slices_array.doAdjacentPairs{
				arg start_frame, end_frame, slice_index;
				var num_frames = end_frame - start_frame;

				"analyzing slice: % / %".format(slice_index + 1,slices_array.size - 1).postln;

				// mfcc analysis, hop over that 0th coefficient because it relates to loudness and here we want to focus on timbre
				FluidBufMFCC.process(s,audio_buffer,start_frame,num_frames,features:features_buf,startCoeff:1,numCoeffs:~nmfccs, numChans: 1).wait;

				// get a statistical summary of the MFCC analysis for this slice
				FluidBufStats.process(s,features_buf,stats:stats_buf).wait;

				// extract and flatten just the 0th frame (numFrames:1) of the statistical summary (because that is the mean)
				FluidBufFlatten.process(s,stats_buf,numFrames:1,destination:flat_buf).wait;

				// now that the means are extracted and flattened, we can add this datapoint to the dataset:
				dataset.addPoint("slice-%".format(slice_index),flat_buf);
			};
		});

		action.value(dataset); // execute the function and pass in the dataset that was created!
	}.play;
};
)

(
// ===================  4. DO THE ANALYSIS =====================
~analyze_to_dataset.(~source_buf,~source_indices_buf,{ // pass in the audio buffer of the source, and the slice points
	arg ds;
	~source_dataset = ds; // set the ds to a global variable so we can access it later
	~source_dataset.print;
});
)

/*
=================================================
|                                               |
|       LOAD AND ANALYZE THE TARGET             |
|                                               |
=================================================
*/

(
// ============= 5. LOAD THE FILE ===================
~target_path = FluidFilesPath("Nicol-LoopE-M.wav");
~target_buf = Buffer.read(s,~target_path);
)

(
// ============= 6. SLICE ===================
~target_indices_buf =  Buffer(s);
FluidBufOnsetSlice.process(s,~target_buf,indices:~target_indices_buf,metric:9,threshold:0.5,action:{
	~target_indices_buf.loadToFloatArray(action:{
		arg indices_array;

		// post the results so that you can tweak the parameters and get what you want
		"found % slices".format(indices_array.size-1).postln;
		"average length: % seconds".format((~target_buf.duration / (indices_array.size-1)).round(0.001)).postln;
	})
});
)

(
// =========== 7. USE THE SAME ANALYSIS FUNCTION
~analyze_to_dataset.(~target_buf,~target_indices_buf,{
	arg ds;
	~target_dataset = ds;
	~target_dataset.print;
});
)

(
// ======================= 8. TEST DRUM LOOP PLAYBACK ====================
// play back the drum slices with a .wait in between so we hear the drum loop
Routine{
	~target_indices_buf.loadToFloatArray(action:{
		arg target_indices_array;

		// prepend 0 (the start  of the file) to the indices array
		target_indices_array = [0] ++ target_indices_array;

		//  append the total number of frames to know how long to play the last slice for
		target_indices_array = target_indices_array ++ [~target_buf.numFrames];


		inf.do{ // loop for infinity
			arg i;

			// get the index to play by modulo one less than the number of slices (we don't want to *start* playing from the
			// last slice point, because that's the end of the file!)
			var index = i % (target_indices_array.size - 1);

			// nb. that the minus one is so that the drum slice from the beginning of the file to the first index is call "-1"
			// this is because that slice didn't actually get analyzed
			var slice_id = index - 1;
			var start_frame = target_indices_array[index];
			var dur_frames = target_indices_array[index + 1] - start_frame;
			var dur_secs = dur_frames / ~target_buf.sampleRate;

			"playing slice: %".format(slice_id).postln;

			{
				var sig = PlayBuf.ar(1,~target_buf,BufRateScale.ir(~target_buf),0,start_frame,0,2);
				var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_secs-0.06,0.03]),doneAction:2);
				// sig = sig * env; // include this env if you like, but keep the line above because it will free the synth after the slice!
				sig.dup;
			}.play;
			dur_secs.wait;
		};
	});
}.play;
)

/*
=================================================
|                                               |
|       KDTREE THE DATA AND DO THE LOOKUP       |
|                                               |
=================================================
*/

(
// ========== 9. FIT THE KDTREE TO THE SOURCE DATASET SO THAT WE CAN QUICKLY LOOKUP NEIGHBORS ===============
Routine{
	~kdtree = FluidKDTree(s);
	~scaled_dataset = FluidDataSet(s);

	// leave only one of these scalers *not* commented-out. try all of them!
	//~scaler = FluidStandardize(s);
	~scaler = FluidNormalize(s);
	// ~scaler = FluidRobustScale(s);

	s.sync;
	~scaler.fitTransform(~source_dataset,~scaled_dataset,{
		~kdtree.fit(~scaled_dataset,{
			"kdtree fit".postln;
		});
	});
}.play;
)

(
// ========= 10. A LITTLE HELPER FUNCTION THAT WILL PLAY BACK A SLICE FROM THE SOURCE BY JUST PASSING THE INDEX =============
~play_source_index = {
	arg index, src_dur;
	{
		var start_frame = Index.kr(~source_indices_buf,index); // lookup the start frame with the index *one the server* using Index.kr
		var end_frame = Index.kr(~source_indices_buf,index+1); // same for the end frame
		var num_frames = end_frame - start_frame;
		var dur_secs = min(num_frames / SampleRate.ir(~source_buf),src_dur);
		var sig = PlayBuf.ar(~loader.buffer.numChannels,~source_buf,BufRateScale.ir(~source_buf),0,start_frame,0,2);
		var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_secs-0.06,0.03]),doneAction:2);
		// sig = sig * env; // include this env if you like, but keep the line above because it will free the synth after the slice!
		sig.dup;
	}.play;
};
)

(
// ======================= 11. QUERY THE DRUM SONDS TO FIND "REPLACEMENTS" ====================
// play back the drum slices with a .wait in between so we hear the drum loop
// is is very similar to step 8 above, but now instead of playing the slice of
// the drum loop, it get's the analysis of the drum loop's slice into "query_buf",
// then uses that info to lookup the nearest neighbour in the source dataset and
// play that slice. If you used, at line 12 above, the FluCoMa sound set, it sounds boringly
// similar: this is because the target drum loop is in the corpus! So it finds, for each slice
// itself... this is a good incentive to reload, with your own soundbank :)

Routine{
	var query_buf = Buffer.alloc(s,~nmfccs); // a buffer for doing the neighbor lookup with
	var scaled_buf = Buffer.alloc(s,~nmfccs);
	~target_indices_buf.loadToFloatArray(action:{
		arg target_indices_array;

		// prepend 0 (the start  of the file) to the indices array
		target_indices_array = [0] ++ target_indices_array;

		//  append the total number of frames to know how long to play the last slice for
		target_indices_array = target_indices_array ++ [~target_buf.numFrames];


		inf.do{ // loop for infinity
			arg i;

			// get the index to play by modulo one less than the number of slices (we don't want to *start* playing from the
			// last slice point, because that's the end of the file!)
			var index = i % (target_indices_array.size - 1);

			// nb. that the minus one is so that the drum slice from the beginning of the file to the first index is call "-1"
			// this is because that slice didn't actually get analyzed
			var slice_id = index - 1;
			var start_frame = target_indices_array[index];
			var dur_frames = target_indices_array[index + 1] - start_frame;

			// this will be used to space out the source slices according to the target timings
			var dur_secs = dur_frames / ~target_buf.sampleRate;

			"target slice: %".format(slice_id).postln;

			// as long as this slice is not the one that starts at the beginning of the file (-1) and
			// not the slice at the end of the file (because neither of these have analyses), let's
			// do the lookup
			if((slice_id >= 0) && (slice_id < (target_indices_array.size - 3)),{

				// use the slice id to (re)create the slice identifier and load the data point into "query_buf"
				~target_dataset.getPoint("slice-%".format(slice_id.asInteger),query_buf,{
					// once it's loaded, scale it using the scaler
					~scaler.transformPoint(query_buf,scaled_buf,{
						// once it's neighbour data point in the kdtree of source slices
						~kdtree.kNearest(scaled_buf,action: {
							arg nearest;

							// peel off just the integer part of the slice to use in the helper function
							var nearest_index = nearest.asString.split($-)[1].asInteger;
							nearest_index.postln;
							~play_source_index.(nearest_index,dur_secs);
						});
					});
				});
			});

			// if you want to hear the drum set along side the neighbor slices, uncomment this function
			/*{
				var sig = PlayBuf.ar(1,~target_buf,BufRateScale.ir(~target_buf),0,start_frame,0,2);
				var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_secs-0.06,0.03]),doneAction:2);
				// sig = sig * env; // include this env if you like, but keep the line above because it will free the synth after the slice!
				sig.dup;
			}.play;*/

			dur_secs.wait;
		};
	});
}.play;
)
