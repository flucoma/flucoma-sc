(
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
			var min = vals.minItem;
			var max = vals.maxItem;
			var range = max - min;

			"min:   %".format(min).postln;
			"max:   %".format(max).postln;
			"range: %".format(range).postln;

			vals = (vals - min) / range;
			vals = (vals * 255).asInteger;

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

FluidBufMelBands.processBlocking(s,~drums,features:~rasterBuffer,numBands:512,windowSize:1024,action:{"done".postln});
FluidBufSTFT.processBlocking(s,~drums,magnitude:~rasterBuffer,windowSize:1024,action:{"done".postln});
FluidBufMFCC.processBlocking(s,~drums,features:~rasterBuffer,windowSize:1024,action:{"done".postln});
FluidBufChroma.processBlocking(s,~drums,features:~rasterBuffer,action:{"done".postln});
~rasterBuffer.postln;

~raster.(~rasterBuffer);