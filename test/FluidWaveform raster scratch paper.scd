(
~normalize = {
	arg vals;
	var min = vals.minItem;
	var max = vals.maxItem;
	var range = max - min;

	"min:   %".format(min).postln;
	"max:   %".format(max).postln;
	"range: %".format(range).postln;

	vals = (vals - min) / range;
	vals = (vals * 255).asInteger;
	vals;
};

~dbs_range = {
	arg vals;
	vals.postln;
	vals = vals / vals.maxItem;
	vals.postln;
	vals = vals.ampdb;
	vals.postln;
	vals = vals.linlin(-120.0,0.0,0.0,255.0).asInteger;
	vals.postln;
	vals;
};

~raster = {
	arg rasterBuffer;
	var colors;

	colors = CSVFileReader.readInterpret(FluidFilesPath("../Resources/color-schemes/%.csv".format("CET-L16"))).collect{
		arg row;
		Color.fromArray(row);
	};

	rasterBuffer.loadToFloatArray(action:{
		arg vals;
		"n vals in raster buffer: %".format(vals.size).postln;
		fork({
			var img = Image(rasterBuffer.numFrames,rasterBuffer.numChannels);
			// vals = ~normalize.(vals);
			vals = ~dbs_range.(vals);

			vals.do{
				arg val, index;
				img.setColor(colors[val], index.div(rasterBuffer.numChannels), rasterBuffer.numChannels - 1 - index.mod(rasterBuffer.numChannels));
			};

			img.plot;
		},AppClock);
	});
};
)

// show mels
~drums = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
~rasterBuffer = Buffer(s);
(
fork({
	Window.closeAll;
	FluidBufMelBands.processBlocking(s,~drums,features:~rasterBuffer,windowSize:4096,hopSize:256,numBands:480,action:{"done".postln});
	// FluidBufSTFT.processBlocking(s,~drums,magnitude:~rasterBuffer,windowSize:1024,action:{"done".postln});
	// FluidBufMFCC.processBlocking(s,~drums,features:~rasterBuffer,windowSize:1024,action:{"done".postln});
	// FluidBufChroma.processBlocking(s,~drums,features:~rasterBuffer,action:{"done".postln});

	s.sync;
	~rasterBuffer.postln;

	~raster.(~rasterBuffer);
},AppClock);
)
