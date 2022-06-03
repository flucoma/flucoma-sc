(
Task{
	var folder = "/Users/macprocomputer/Desktop/_flucoma/data_saves/211103_152953_2D_browsing_MFCC/";
	// var folder = "/Users/macprocomputer/Desktop/_flucoma/data_saves/211103_161354_2D_browsing_SpectralShape/";
	// var folder = "/Users/macprocomputer/Desktop/_flucoma/data_saves/211103_161638_2D_browsing_Pitch/";
	~ds_original = FluidDataSet(s);
	~buffer = Buffer.read(s,folder+/+"buffer.wav");
	~indices = Buffer.read(s,folder+/+"indices.wav");
	~kdtree = FluidKDTree(s,6);
	~ds = FluidDataSet(s);

	s.sync;

	~indices.loadToFloatArray(action:{
		arg fa;
		~indices = fa;
	});

	~ds_original.read(folder+/+"ds.json",{
		~ds.read(folder+/+"ds.json",{
			~kdtree.fit(~ds,{
				~ds.dump({
					arg dict;
					~ds_dict = dict;
					"kdtree fit".postln;
				});
			});
		});
	});
}.play;

~play_id = {
	arg id;
	var index = id.asString.split($-)[1].asInteger;
	var start_samps = ~indices[index];
	var end_samps = ~indices[index+1];
	var dur_secs = (end_samps - start_samps) / ~buffer.sampleRate;
	{
		var sig = PlayBuf.ar(1,~buffer,BufRateScale.ir(~buffer),startPos:start_samps);
		var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_secs-0.06,0.03]),doneAction:2);
		sig.dup;// * env;
	}.play;
	dur_secs;
};
~pt_buf = Buffer.alloc(s,~ds_dict.at("cols"));
)

(
// hear the 5 nearest points
Routine{
	// var id = "slice-558";
	var id = ~ds_dict.at("data").keys.choose;
	~ds.getPoint(id,~pt_buf,{
		~kdtree.kNearest(~pt_buf,{
			arg nearest;
			Routine{
				id.postln;
				~play_id.(id).wait;
				nearest[1..].do{
					arg near;
					1.wait;
					near.postln;
					~play_id.(near).wait;
				};
			}.play;
		})
	});
}.play;
)

// Standardize
(
Routine{
	var scaler = FluidStandardize(s);
	s.sync;
	scaler.fitTransform(~ds_original,~ds,{
		~kdtree.fit(~ds,{
			"standardized & kdtree fit".postln;
		});
	});
}.play;
)

// Normalize
(
Routine{
	var scaler = FluidNormalize(s);
	s.sync;
	scaler.fitTransform(~ds_original,~ds,{
		~kdtree.fit(~ds,{
			"normalized & kdtree fit".postln;
		});
	});
}.play;
)

// Robust Scaler
(
Routine{
	var scaler = FluidRobustScale(s);
	s.sync;
	scaler.fitTransform(~ds_original,~ds,{
		~kdtree.fit(~ds,{
			"normalized & kdtree fit".postln;
		});
	});
}.play;
)