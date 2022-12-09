(
// Window.closeAll;
s.waitForBoot{

	Task{
		var buf = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
		var slicepoints = Buffer(s); // FluidBufAmpSlice will write into this buffer the samples at which slices are detected.
		var features_buf = Buffer(s); // a buffer for writing the analysis from FluidSpectralShape into
		var stats_buf = Buffer(s); // a buffer for writing the statistic analyses into
		var point_buf = Buffer(s,2); // a buffer that will be used to add points to the dataset - the analyses will be written into this buffer first
		var ds = FluidDataSet(s); // a data set for storing the analysis of each slice (mean centroid & mean loudness)
		var scaler = FluidNormalize(s); // a tool for normalizing a dataset (making it all range between zero and one)
		var kdtree = FluidKDTree(s); // a kdtree for fast nearest neighbour lookup

		s.sync;

		FluidBufAmpSlice.processBlocking(s,buf,indices:slicepoints,fastRampUp:10,fastRampDown:2205,slowRampUp:4410,slowRampDown:4410,onThreshold:10,offThreshold:5,floor:-40,minSliceLength:4410,highPassFreq:20);
		// slice the drums buffer based on amplitude
		// the samples at which slices are detected will be written into the "slicepoints" buffer

		s.sync;

		FluidWaveform(buf,slicepoints,bounds:Rect(0,0,1600,400));
		// plot the drums buffer with the slicepoints overlayed

		slicepoints.loadToFloatArray(action:{ // bring the values in the slicepoints buffer from the server to the language as a float array
			arg slicepoints_fa; // fa stands for float array
			slicepoints_fa.postln;
			slicepoints_fa.doAdjacentPairs{
				/*
				take each of the adjacent pairs and pass them to this function as an array of 2 values

				nb. for example [0,1,2,3,4] will execute this function 4 times, passing these 2 value arrays:
				[0,1]
				[1,2]
				[2,3]
				[3,4]

				this will give us each slice point *and* the next slice point so that we
				can tell the analyzers where to start analyzing and how many frames to analyze
				*/
				arg start_samps, end_samps, slice_i;
				var num_samps = end_samps - start_samps; // the next slice point minus the current one will give us the difference how many slices to analyze)

				slice_i.postln; // post which slice index we're currently analyzing

				// the ".wait"s will pause the Task (that this whole things is in) until the analysis is done;

				FluidBufSpectralShape.process(s,buf,start_samps,num_samps,features:features_buf).wait;
				/* analyze the drum buffer starting at `start_samps` and for `num_samps` samples
				this returns a buffer (feautres_buf) that is 7 channels wide (for the 7 spectral analyses, see helpfile) and
				however many frames long as there are fft frames in the slice */

				FluidBufStats.process(s,features_buf,numChans:1,stats:stats_buf).wait;
				/* perform a statistical analysis the spectral analysis, doing only the first channel (specified by `numChans:1`)
				this will return just one channel because we asked it to analyze only 1 channel. that one channel will have 7 frames
				corresponding to the 7 statistical analyses that it performs */

				FluidBufCompose.process(s,stats_buf,0,1,destination:point_buf,destStartFrame:0).wait;
				/* FluidBufCompose is essentially a "buf copy" operation. this will copy just the zeroth frame from `stats_buf` (mean)
				into the zeroth buf of `point_buf` which is what we'll evenutally use to add the data to the dataset */

				FluidBufLoudness.process(s,buf,start_samps,num_samps,features:features_buf).wait;
				// do a loudness analysis

				FluidBufStats.process(s,features_buf,numChans:1,stats:stats_buf).wait;
				// see above

				FluidBufCompose.process(s,stats_buf,0,1,destination:point_buf,destStartFrame:1).wait;
				/* see above, but this time the mean loudnessi s being copied into the 1st frame of `point_buf` so that it doesn't overwrite the mean centroid */

				ds.addPoint("point-%".format(slice_i),point_buf);
				/* now that we've added the mean centroid and mean loudness into `point_buf`, we can use that buf to add the data that is in it to the dataset.
				we also need to give it an identifer. here we're calling it "point-%", where the "%" is replaced by the index of the slice */

				s.sync;
			};
		});

		scaler.fitTransform(ds,ds,{
			/* scale the dataset so that each dimension is scaled to between 0 and 1. this will do this operation "in place", so that once the
			scaling is done on the dataset "ds" it will overwrite that dataset with the normalized values. that is why both the "sourceDataSet" and
			"destDataSet" are the same here
			*/

			kdtree.fit(ds,{ // fit the kdtree to the (now) normalized dataset
				ds.dump({ // dump out that dataset to dictionary so that we can use it with the plotter!
					arg ds_dict;// the dictionary version of this dataset
					var previous = nil; // a variable for checking if the currently passed nearest neighbour is the same or different from the previous one
					{FluidPlotter(bounds:Rect(0,0,800,800),dict:ds_dict,mouseMoveAction:{
						/* make a FluidPlotter. nb. the dict is the dict from a FluidDataSet.dump. the mouseMoveAction is a callback function that is called
						anytime the mouseDownAction or mouseMoveAction function is called on this view. i.e., anytime you click or drag on this plotter */

						arg view, x, y, modifiers;
						/* the function is passed:
						(1) itself
						(2) mouse x position (scaled to what the view's scales are)
						(3) mouse y position (scaled to what the view's scales are)
						(4) modifier keys that are pressed while clicking or dragging
						*/
						point_buf.setn(0,[x,y]); // write the x y position into a buffer so that we can use it to...
						kdtree.kNearest(point_buf, action:{ // look up the nearest slice to that x y position
							arg nearest; // this is reported back as a symbol, so...
							nearest = nearest.asString; // we'll convert it to a string here

							if(nearest != previous,{
								/* if it's not the last one that was found, we can do something with it. this
								is kind of like a debounce. we just don't want to retrigger this action each time a drag
								happens if it is actually the same nearest neighbor*/

								var index = nearest.split($-)[1].interpret;
								// split at the hyphen and interpret the integer on the end to find out what slice index it is

								{
									var startPos = Index.kr(slicepoints,index); // look up the start sample based on the index
									var endPos = Index.kr(slicepoints,index + 1); // look up the end sample based on the index
									var dur_secs = (endPos - startPos) / BufSampleRate.ir(buf); // figure out how long it is in seconds to create an envelope
									var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_secs-0.06,0.03]),doneAction:2);
									var sig = PlayBuf.ar(1,buf,BufRateScale.ir(buf),startPos:startPos);
									sig.dup * env;
								}.play; // play it!

								view.highlight_(nearest); // make this point a little bit bigger in the plot
								previous = nearest;
							});
						});
					})}.defer;
				});
			});
		});
	}.play(AppClock);
}
)
