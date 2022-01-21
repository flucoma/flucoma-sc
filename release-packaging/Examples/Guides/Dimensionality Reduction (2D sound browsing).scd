(
// 1. define a function to load a folder of sounds
~load_folder = {
	arg folder_path, action;
	var loader = FluidLoadFolder(folder_path);
	loader.play(s,{
		fork{
			var mono_buffer = Buffer.alloc(s,loader.buffer.numFrames); // convert to mono for ease of use for this example
			FluidBufCompose.processBlocking(s,loader.buffer,destination:mono_buffer,numChans:1);
			s.sync;
			action.(mono_buffer);
		}
	});
};

~load_folder.(File.realpath(FluidBufPitch.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/",{
	arg buffer;
	"mono buffer: %".format(buffer).postln;
	~buffer = buffer;
});
)

(
// 2. define a function to slice the sounds, play with the threshold to get different results
~slice = {
	arg buffer, action;
	Routine{
		var indices = Buffer(s);
		s.sync;
		FluidBufNoveltySlice.process(s,buffer,indices:indices,threshold:0.5,action:{
			"% slices found".format(indices.numFrames).postln;
			"average duration in seconds: %".format(buffer.duration/indices.numFrames).postln;
			action.(buffer,indices);
		});
	}.play;
};

~slice.(~buffer,{
	arg buffer, indices;
	~indices = indices;
});
)

(
// 3. analyze the slices
~analyze = {
	arg buffer, indices, action;
	var time = SystemClock.seconds;
	Routine{
		var feature_buf = Buffer(s);
		var stats_buf = Buffer(s);
		var point_buf = Buffer(s);
		var ds = FluidDataSet(s);

		indices.loadToFloatArray(action:{
			arg fa;
			fa.doAdjacentPairs{
				arg start, end, i;
				var num = end - start;

				FluidBufMFCC.processBlocking(s,buffer,start,num,features:feature_buf,numCoeffs:13,startCoeff:1);
				FluidBufStats.processBlocking(s,feature_buf,stats:stats_buf);
				FluidBufFlatten.processBlocking(s,stats_buf,numFrames:1,destination:point_buf);

				ds.addPoint("slice-%".format(i),point_buf);
				"Processing Slice % / %".format(i+1,indices.numFrames-1).postln;
			};

			s.sync;

			feature_buf.free; stats_buf.free; point_buf.free;

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
		var standardizer = FluidStandardize(s);
		var umap = FluidUMAP(s,2,numNeighbours,minDist);

		var redux_ds = FluidDataSet(s);

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
		var normer = FluidNormalize(s);
		var grider = FluidGrid(s);
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
		var kdtree = FluidKDTree(s);
		var buf_2d = Buffer.alloc(s,2);
		var scaler = FluidNormalize(s);
		var newds = FluidDataSet(s);
		var xmin = 0, xmax = 1, ymin = 0, ymax = 1;

		s.sync;

		scaler.fitTransform(redux_ds,newds,{
			"scaling done".postln;
			kdtree.fit(newds,{
				"kdtree fit".postln;
				newds.dump({
					arg dict;
					var previous, fp;
					"ds dumped".postln;
					fp = FluidPlotter(nil,Rect(0,0,800,800),dict,xmin:xmin,xmax:xmax,ymin:ymin,ymax:ymax,mouseMoveAction:{
						arg view, x, y;
						[x,y].postln;
						buf_2d.setn(0,[x,y]);
						kdtree.kNearest(buf_2d,{
							arg nearest;
							if(previous != nearest,{
								var index = nearest.asString.split($-)[1].asInteger;
								previous = nearest;
								nearest.postln;
								index.postln;
								{
									var startPos = Index.kr(indices,index);
									var dur_samps = Index.kr(indices,index + 1) - startPos;
									var sig = PlayBuf.ar(1,buffer,BufRateScale.ir(buffer),startPos:startPos);
									var dur_sec = dur_samps / BufSampleRate.ir(buffer);
									var env;
									dur_sec = min(dur_sec,1);
									env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_sec-0.06,0.03]),doneAction:2);
									sig.dup * env;
								}.play;
							});
						});
					});
					action.(fp,newds);
				});
			});
		});
	}.play;
};

~plot.(~buffer,~indices,~ds);
)

// ============== do all of it =======================
(
var path = "/Users/macprocomputer/Desktop/_flucoma/data_saves/%_2D_browsing_Pitch".format(Date.localtime.stamp);
~load_folder.("/Users/macprocomputer/Desktop/_flucoma/favs mono/",{
	arg buffer0;
	~slice.(buffer0,{
		arg buffer1, indices1;
		~analyze.(buffer1, indices1,{
			arg buffer2, indices2, ds2;

			/*			path.mkdir;
			buffer2.write(path+/+"buffer.wav","wav");
			indices2.write(path+/+"indices.wav","wav","float");
			ds2.write(path+/+"ds.json");*/

			~umap.(buffer2,indices2,ds2,{
				arg buffer3, indices3, ds3;

				/*				path.mkdir;
				buffer3.write(path+/+"buffer.wav","wav");
				indices3.write(path+/+"indices.wav","wav","float");
				ds3.write(path+/+"ds.json");*/

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

/*=============== Know Your Data =================

hmmm... there's a lot of white space in that UMAP plot. A few options:

1. Adjust the parameters of UMAP to make the plot look different.
- minDist
- numNeighbours
2. Gridify the whole thing to spread it out.
3. Remove some of the outliers to get a more full shape.

===================================================*/

// #2
(
Window.closeAll;
Task{
	var folder = "/Users/macprocomputer/Desktop/_flucoma/data_saves/211103_121441_2D_browsing/";
	var ds = FluidDataSet(s);
	var buffer = Buffer.read(s,folder+/+"buffer.wav");
	var indices = Buffer.read(s,folder+/+"indices.wav");
	var normalizer = FluidNormalize(s);
	var ds_grid = FluidDataSet(s);
	var grid = FluidGrid(s);
	var kdtree = FluidKDTree(s);
	var pt_buf = Buffer.alloc(s,2);

	s.sync;

	ds.read(folder+/+"ds.json",{
		"read".postln;
		normalizer.fitTransform(ds,ds_grid,{
			"normalized".postln;
			grid.fitTransform(ds_grid,ds_grid,{
				"grid done".postln;
				normalizer.fitTransform(ds_grid,ds_grid,{
					"normalized".postln;
					kdtree.fit(ds_grid,{
						"tree fit".postln;
						normalizer.fitTransform(ds,ds,{
							"normalized".postln;
							ds.dump({
								arg ds_dict;
								ds_grid.dump({
									arg ds_grid_dict;

									defer{
										var distances = Dictionary.new;
										var max_dist = 0;
										var win, plotter, uv;

										var previous;
										ds_dict.at("data").keysValuesDo({
											arg id, pt;
											var other, pt0, pt1, dist, distpoint;

											/*
											id.postln;
											pt.postln;
											"".postln;
											*/

											other = ds_grid_dict.at("data").at(id);
											pt0 = Point(pt[0],pt[1]);
											pt1 = Point(other[0],other[1]);
											dist = pt0.dist(pt1);
											distpoint = Dictionary.new;

											if(dist > max_dist,{max_dist = dist});

											distpoint.put("pt0",pt0);
											distpoint.put("pt1",pt1);
											distpoint.put("dist",dist);
											distances.put(id,distpoint);
										});
										win = Window("FluidGrid",Rect(0,0,800,800));
										win.background_(Color.white);
										uv = UserView(win,win.bounds)
										.drawFunc_({
											var size_pt = Point(uv.bounds.width,uv.bounds.height);

											distances.keysValuesDo({
												arg id, distpoint;
												var alpha = distpoint.at("dist") / max_dist;
												var pt0 = distpoint.at("pt0") * size_pt;
												var pt1 = distpoint.at("pt1") * size_pt;

												pt0.y = uv.bounds.height - pt0.y;
												pt1.y = uv.bounds.height - pt1.y;

												/*											id.postln;
												distpoint.postln;
												alpha.postln;
												"".postln;
												*/

												Pen.line(pt0,pt1);
												Pen.color_(Color(1.0,0.0,0.0,0.25));
												Pen.stroke;
											});
										});

										plotter = FluidPlotter(win,win.bounds,ds_dict,{
											arg view, x, y;
											pt_buf.setn(0,[x,y]);
											kdtree.kNearest(pt_buf,{
												arg nearest;
												if(previous != nearest,{
													var index = nearest.asString.split($-)[1].asInteger;
													previous = nearest;
													nearest.postln;
													index.postln;
													{
														var startPos = Index.kr(indices,index);
														var dur_samps = Index.kr(indices,index + 1) - startPos;
														var sig = PlayBuf.ar(1,buffer,BufRateScale.ir(buffer),startPos:startPos);
														var dur_sec = dur_samps / BufSampleRate.ir(buffer);
														var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_sec-0.06,0.03]),doneAction:2);
														sig.dup * env;
													}.play;
												});
											});
										});
										plotter.background_(Color(0,0,0,0));

										ds_grid_dict.at("data").keysValuesDo({
											arg id, pt;
											plotter.addPoint_("%-grid".format(id),pt[0],pt[1],0.75,Color.blue.alpha_(0.5));
										});

										win.front;
									};
								})
							});
						});
					});
				});
			});
		});
	});
}.play(AppClock);
)

// #3
(
Routine{
	var folder = "/Users/macprocomputer/Desktop/_flucoma/data_saves/211103_152523_2D_browsing/";
	var ds = FluidDataSet(s);
	var buffer = Buffer.read(s,folder+/+"buffer.wav");
	var indices = Buffer.read(s,folder+/+"indices.wav");
	var robust_scaler = FluidRobustScale(s,10,90);
	var newds = FluidDataSet(s);
	var dsq = FluidDataSetQuery(s);
	s.sync;

	// {indices.plot}.defer;
	ds.read(folder+/+"ds.json",{
		robust_scaler.fitTransform(ds,newds,{
			dsq.addRange(0,2,{
				dsq.filter(0,">",-1,{
					dsq.and(0,"<",1,{
						dsq.and(1,">",-1,{
							dsq.and(1,"<",1,{
								dsq.transform(newds,newds,{
									~plot.(buffer,indices,newds);
								});
							});
						});
					});
				});
			});
		})
	});
}.play;
)