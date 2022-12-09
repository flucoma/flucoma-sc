(
// run the analysis
Routine{
	var time = Main.elapsedTime;
	var ds = FluidDataSet(s);
	var labels = FluidLabelSet(s);
	var scaler = FluidStandardize(s);
	var buf1 = Buffer.alloc(s,1);
	var dsq = FluidDataSetQuery(s);

	~pitch_features_buf = Buffer.new(s);
	// specify some params for the analysis (these are the defaults, but we'll specify them here so we can use them later)
	~windowSize = 4096;
	~hopSize = 512;

	~buf = Buffer.read(s,FluidFilesPath("Tremblay-FMTriDist-M.wav"));

	s.sync;

	FluidBufPitch.process(s,~buf,features:~pitch_features_buf,windowSize:~windowSize,hopSize:~hopSize).wait;
	// {~pitch_features_buf.plot(separately:true)}.defer;

	ds.fromBuffer(~pitch_features_buf,action:{
		ds.print;
		/*dsq.addRange(0,2,{
		dsq.filter(1,">",0.7,{
		dsq.transform(ds,ds,{
		ds.print;*/
		ds.dump({
			arg dict;
			~pitch_features_array = Array.newClear(dict.at("data").size);
			dict.at("data").keysValuesDo({
				arg id, pt, i;
				~pitch_features_array[i] = [id,pt];
			});

			~pitch_features_sorted = ~pitch_features_array.sort({
				arg a, b;
				a[1][0] < b[1][0];
			});

			~center_pos = ~pitch_features_sorted.collect({arg arr; (arr[0].asInteger * ~hopSize) / ~buf.sampleRate});

			~center_pos_buf = Buffer.loadCollection(s,~center_pos);
		});
		/*});
		});
		});*/
	});
}.play
)

(
OSCdef(\fluidbufpitch_help,{
	arg msg;
	msg[3].midiname.postln;
},"/fluidbufpitch_help");

{
	var trig = Impulse.kr(s.sampleRate / ~hopSize);
	var index = (PulseCount.kr(trig) - 1) % BufFrames.ir(~center_pos_buf);
	var centerPos = Index.kr(~center_pos_buf,index);
	var pan = TRand.kr(-1.0,1.0,trig);
	var sig;
	var pitch, conf;
	sig = TGrains.ar(2,trig,~buf,BufRateScale.ir(~buf),centerPos,~windowSize / BufSampleRate.ir(~buf),pan,0.5);
	# pitch, conf = FluidPitch.kr(sig,unit:1,windowSize:4096);
	pitch = FluidStats.kr(pitch,25)[0];
	SendReply.kr(Impulse.kr(30) * (conf > 0.6),"/fluidbufpitch_help",pitch);
	sig;
}.play;
)