/*

this script shows how to

1. load a folder of sounds
2. find smaller time segments within the sounds according to novelty
3. analyse the sounds according to MFCC and add these analyses to a dataset
4. dimensionally reduce that dataset to 2D using umap
5. (optional) turn the plot of points in 2D into a grid
6. plot the points!

notice that each step in this process is created within a function so that
at the bottom of the patch, these functions are all chained together to
do the whole process in one go!

*/

(
// 1. load a folder of sounds
~load_folder = {
	arg folder_path, action;
	var loader = FluidLoadFolder(folder_path); // pass in the folder to load
	loader.play(s,{ // play will do the actual loading
		var mono_buffer = Buffer.alloc(s,loader.buffer.numFrames);
		FluidBufCompose.processBlocking(s,loader.buffer,destination:mono_buffer,numChans:1,action:{
			action.(mono_buffer);
		});
	});
};

// this will load all the audio files that are included with the flucoma toolkit, but you can put your own path here:
~load_folder.(FluidFilesPath(),{
	arg buffer;
	"mono buffer: %".format(buffer).postln;
	~buffer = buffer; // save the buffer to a global variable so we can use it later
});
)

(
// 2. slice the sounds
~slice = {
	arg buffer, action;
	var indices = Buffer(s); // a buffer for saving the discovered indices into

	// play around the the threshold anad feature (see help file) to get differet slicing results
	FluidBufNoveltySlice.processBlocking(s,buffer,indices:indices,algorithm:0,threshold:0.5,action:{
		"% slices found".format(indices.numFrames).postln;
		"average duration in seconds: %".format(buffer.duration/indices.numFrames).postln;
		action.(buffer,indices);
	});
};

~slice.(~buffer,{
	arg buffer, indices;
	~indices = indices;
});
)

// you may want to check the slice points here using FluidWaveform
FluidWaveform(~buffer,~indices); // it may also be way too many slices to see properly!

(
// 3. analyze the slices
~analyze = {
	arg buffer, indices, action;
	var time = SystemClock.seconds; // a timer just to keep tabs on how long this stuff is taking
	Routine{
		var feature_buf = Buffer(s); // a buffer for storing the mfcc analyses into
		var stats_buf = Buffer(s); // a buffer for storing the stats into
		var point_buf = Buffer(s); // a buffer we will use to add points to the dataset
		var ds = FluidDataSet(s); // the dataset that we'll add all these mfcc analyses to

		// bring the values in the slicepoints buffer from the server to the language as a float array
		indices.loadToFloatArray(action:{
			arg fa; // float array
			fa.doAdjacentPairs{
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
				arg start, end, i;

				// the next slice point minus the current one will give us the difference how many slices to analyze)
				var num = end - start;

				/* analyze the drum buffer starting at `start_samps` and for `num_samps` samples
				this returns a buffer (feautre_buf) that is 13 channels wide (for the 13 mfccs, see helpfile) and
				however many frames long as there are fft frames in the slice */
				FluidBufMFCC.processBlocking(s,buffer,start,num,features:feature_buf,numCoeffs:13,startCoeff:1);

				/* perform a statistical analysis on the mfcc analysis
				this will return just 13 channels, one for each mfcc channel in the feature_buf.
				each channel will have 7 frames	corresponding to the 7 statistical analyses that it performs
				on that channel */
				FluidBufStats.processBlocking(s,feature_buf,stats:stats_buf);

				/* take all 13 channels from stats_buf, but just the first frame (mean) and convert it into a buffer
				that is 1 channel and 13 frames. this shape will be considered "flat" and therefore able to be
				added to the dataset */
				FluidBufFlatten.processBlocking(s,stats_buf,numFrames:1,destination:point_buf);

				// add it
				ds.addPoint("slice-%".format(i),point_buf);
				"Processing Slice % / %".format(i+1,indices.numFrames-1).postln;
			};

			s.sync;

			feature_buf.free; stats_buf.free; point_buf.free; // free buffers

			ds.print;

			"Completed in % seconds".format(SystemClock.seconds - time).postln;
			action.(buffer,indices,ds);
		});
	}.play;
};

~analyze.(~buffer,~indices,{
	arg buffer, indices, ds;
	~ds = ds;
});
)

(
// 4. Reduce to 2 Dimensions
~umap = {
	arg buffer, indices, ds, action, numNeighbours = 15, minDist = 0.1;
	Routine{

		// get all the dimensions in the same general range so that when umap
		// makes its initial tree structure, the lower order mfcc coefficients
		// aren't over weighted
		var standardizer = FluidStandardize(s);

		// this is the dimensionality reduction algorithm, see helpfile for
		// more info
		var umap = FluidUMAP(s,2,numNeighbours,minDist);

		var redux_ds = FluidDataSet(s); // a new dataset for putting the 2D points into

		s.sync;

		standardizer.fitTransform(ds,redux_ds,{
			"standardization done".postln;
			umap.fitTransform(redux_ds,redux_ds,{
				"umap done".postln;
				action.(buffer,indices,redux_ds);
			});
		});
	}.play;
};

~umap.(~buffer,~indices,~ds,{
	arg buffer, indices, redux_ds;
	~ds = redux_ds;
});
)

(
// 5. Gridify if Desired
~grid = {
	arg buffer, indices, redux_ds, action;
	Routine{

		// first normalize so they're all 0 to 1
		var normer = FluidNormalize(s);

		// this will shift all dots around so they're in a grid shape
		var grider = FluidGrid(s);

		// a new dataset to hold the gridified dots
		var newds = FluidDataSet(s);

		s.sync;

		normer.fitTransform(redux_ds,newds,{
			"normalization done".postln;
			grider.fitTransform(newds,newds,{
				"grid done".postln;
				action.(buffer,indices,newds);
			});
		});
	}.play;
};

~grid.(~buffer,~indices,~ds,{
	arg buffer, indices, grid_ds;
	~ds = grid_ds;
});
)

(
// 6. Plot
~plot = {
	arg buffer, indices, redux_ds, action;
	Routine{
		var kdtree = FluidKDTree(s); // tree structure of the 2D points for fast neighbour lookup

		// a buffer for putting the 2D mouse point into so that it can be used to find the nearest neighbour
		var buf_2d = Buffer.alloc(s,2);

		// scaler just to double check and make sure that the points are 0 to 1
		// if the plotter is receiving the output of umap, they probably won't be...
		var scaler = FluidNormalize(s);

		// a new dataset told the normalized data
		var newds = FluidDataSet(s);

		s.sync;

		scaler.fitTransform(redux_ds,newds,{
			"scaling done".postln;
			kdtree.fit(newds,{
				"kdtree fit".postln;
				newds.dump({
					arg dict;
					var previous, fp;
					"ds dumped".postln;

					// pass in the dict from the dumped dataset. this is the data that we want to plot!

					{
						fp = FluidPlotter(nil,Rect(0,0,800,800),dict,mouseMoveAction:{

							// when the mouse is clicked or dragged on the plotter, this function executes

							// the view is the FluidPlotter, the x and y are the position of the mouse according
							// to the range of the plotter. i.e., since our plotter is showing us the range 0 to 1
							// for both x and y, the xy positions will always be between 0 and 1
							arg view, x, y;
							buf_2d.setn(0,[x,y]); // set the mouse position into a buffer

							// then send that buffer to the kdtree to find the nearest point
							kdtree.kNearest(buf_2d,action:{
								arg nearest; // the identifier of the nearest point is returned (always as a symbol)

								if(previous != nearest,{ // as long as this isn't also the last one that was returned

									// split the integer off the indentifier to know how to look it up for playback
									var index = nearest.asString.split($-)[1].asInteger;
									previous = nearest;
									nearest.postln;
									// index.postln;
									{
										var startPos = Index.kr(indices,index); // look in the indices buf to see where to start playback
										var dur_samps = Index.kr(indices,index + 1) - startPos; // and how long
										var sig = PlayBuf.ar(1,buffer,BufRateScale.ir(buffer),startPos:startPos);
										var dur_sec = dur_samps / BufSampleRate.ir(buffer);
										var env;
										dur_sec = min(dur_sec,1); // just in case some of the slices are *very* long...
										env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_sec-0.06,0.03]),doneAction:2);
										sig.dup * env;
									}.play;
								});
							});
						});
						action.(fp,newds);
					}.defer;
				});
			});
		});
	}.play;
};

~plot.(~buffer,~indices,~ds);
)

// ============== do all of it in one go (without the grid for instance) =======================
(
var path = FluidFilesPath();
~load_folder.(path,{
	arg buffer0;
	~slice.(buffer0,{
		arg buffer1, indices1;
		~analyze.(buffer1, indices1,{
			arg buffer2, indices2, ds2;
			~umap.(buffer2,indices2,ds2,{
				arg buffer3, indices3, ds3;
				~plot.(buffer3,indices3,ds3,{
					arg plotter;
					"done with all".postln;
					~fp = plotter;
				});
			});
		});
	});
});
)
