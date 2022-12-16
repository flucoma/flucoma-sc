(
s.waitForBoot{
	//var audio = Buffer.readChannel(s,FluidFilesPath("Tremblay-CEL-GlitchyMusicBoxMelo.wav"),channels:[0]);
	// var audio = Buffer.readChannel(s,FluidFilesPath("Nicol-LoopE-M.wav"),channels:[0]);
	 var audio = Buffer.readChannel(s,FluidFilesPath("Harker-DS-TenOboeMultiphonics-M.wav"),channels:[0]);

	var window = Window(bounds:Rect(0,0,1600,400));
	var mels = Buffer(s);
	var mags = Buffer(s);
	var chroma = Buffer(s);
	var mfccs = Buffer(s);
	var addLayer = {
		arg win, imageBuffer, color, imageAlpha, colorScaling;
		imageBuffer.loadToFloatArray(action:{
			arg vals;
			fork({
				var colors = CSVFileReader.readInterpret(FluidFilesPath("../color-schemes/%.csv".format(color))).collect{
					arg row;
					Color.fromArray(row);
				};
				var img = Image(imageBuffer.numFrames,imageBuffer.numChannels);

				colorScaling.switch(
					0,{
						var minItem = vals.minItem;
						vals = (vals - minItem) / (vals.maxItem - minItem);
						vals = (vals * 255).asInteger;
					},
					1,{
						vals = (vals + 1e-6).log;
						vals = vals.linlin(vals.minItem,vals.maxItem,0.0,255.0).asInteger;
						// vals.postln;
					},
					{
						"% colorScaling argument % is invalid.".format(thisMethod,colorScaling).warn;
					}
				);

				vals.do{
					arg val, index;
/*					img.postln;
					index.postln;
					val.postln;*/
					img.setColor(colors[val], index.div(imageBuffer.numChannels), imageBuffer.numChannels - 1 - index.mod(imageBuffer.numChannels));
				};

				UserView(win,Rect(0,0,win.bounds.width,win.bounds.height))
				.drawFunc_{
					img.drawInRect(Rect(0,0,win.bounds.width,win.bounds.height),fraction:imageAlpha);
				};
			},AppClock)
		});
	};

	FluidBufMelBands.processBlocking(s,audio,features:mels);
	FluidBufSTFT.processBlocking(s,audio,magnitude:mags);
	FluidBufChroma.processBlocking(s,audio,features:chroma);
	FluidBufMFCC.processBlocking(s,audio,features:mfccs);

	s.sync;

	"analysis done".postln;

	// addLayer.(window,imageBuffer:mags,color:"CET-L02",imageAlpha:0.7,colorScaling:1);
	addLayer.(window,imageBuffer:mels,color:"CET-L02",imageAlpha:1,colorScaling:1);

	// addLayer.(window,imageBuffer:chroma,color:"CET-L16",imageAlpha:0.5,colorScaling:1);
	addLayer.(window,imageBuffer:mfccs,color:"CET-L16",imageAlpha:0.3,colorScaling:0);

	window.front;
};
)