FluidViewer {
	var categoryColors;

	createCatColors {
		categoryColors = "1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf".clump(6).collect{
			arg six;
			Color(*six.clump(2).collect{
				arg two;
				"0x%".format(two).interpret / 255;
			});
		};
	}
}

FluidWaveform : FluidViewer {
	var <win;

	*new {
		arg audioBuffer, slicesBuffer, featureBuffer, bounds, lineWidth = 1, waveformColor, stackFeatures = false, spectrogram = false;
		^super.new.init(audioBuffer,slicesBuffer, featureBuffer, bounds, lineWidth, waveformColor,stackFeatures,spectrogram);
	}

	init {
		arg audio_buf, slices_buf, feature_buf, bounds, lineWidth, waveformColor,stackFeatures = false, spectrogram = false;
		Task{
			var sfv, categoryCounter = 0;

			waveformColor = waveformColor ? Color(*0.5.dup(3));

			this.createCatColors;

			bounds = bounds ? Rect(0,0,800,200);
			win = Window("FluidWaveform",bounds);
			win.background_(Color.white);

			if(audio_buf.notNil,{
				if(spectrogram,{
					var magsbuf = Buffer(audio_buf.server);
					var condition = Condition.new;
					var colors;

					if(File.exists("/Users/macprocomputer/Desktop/_flucoma/code/flucoma-sc/test/CETperceptual_csv_0_1/CET-L16.csv").not,{
						"Sorry, colors file doesn't exist...... where can I put this file so it exists for everyone!!!?!?!?!?!?".warn;
					});

					colors = CSVFileReader.readInterpret("/Users/macprocomputer/Desktop/_flucoma/code/flucoma-sc/test/CETperceptual_csv_0_1/CET-L16.csv").collect{
						arg row;
						Color.fromArray(row);
					};

					FluidBufSTFT.processBlocking(audio_buf.server,audio_buf,magnitude:magsbuf,action:{
						magsbuf.loadToFloatArray(action:{
							arg mags;
							fork({
								var img = Image(magsbuf.numFrames,magsbuf.numChannels);
								mags = (mags / mags.maxItem).ampdb.linlin(-120.0,0.0,0,255).asInteger;

								mags.do{
									arg mag, index;
									img.setColor(colors[mag], index.div(magsbuf.numChannels), magsbuf.numChannels - 1 - index.mod(magsbuf.numChannels));
								};

								UserView(win,Rect(0,0,win.bounds.width,win.bounds.height))
								.drawFunc_{
									img.drawInRect(Rect(0,0,win.bounds.width,win.bounds.height));
								};

								condition.unhang;
							},AppClock)
						});
					});
					condition.hang;
				},{
					var path = "%%_%_FluidWaveform.wav".format(PathName.tmp,Date.localtime.stamp,UniqueID.next);

					audio_buf.write(path,"wav");

					audio_buf.server.sync;

					sfv = SoundFileView(win,Rect(0,0,bounds.width,bounds.height));
					sfv.peakColor_(waveformColor);
					// sfv.rmsColor_(Color.black);
					sfv.rmsColor_(Color.clear);
					sfv.background_(Color.white);
					sfv.readFile(SoundFile(path));
					sfv.gridOn_(false);

					File.delete(path);
				});
			});

			if(slices_buf.notNil,{
				slices_buf.numChannels.switch(
					1,{
						slices_buf.loadToFloatArray(action:{
							arg slices_fa;
							UserView(win,Rect(0,0,bounds.width,bounds.height))
							.drawFunc_({
								Pen.width_(lineWidth);
								slices_fa.do{
									arg start_samp;
									var x = start_samp.linlin(0,audio_buf.numFrames,0,bounds.width);
									Pen.line(Point(x,0),Point(x,bounds.height));
									Pen.color_(categoryColors[categoryCounter]);
									Pen.stroke;
								};
								categoryCounter = categoryCounter + 1;
							});
						});
					},
					2,{
						slices_buf.loadToFloatArray(action:{
							arg slices_fa;
							slices_fa = slices_fa.clump(2);
							UserView(win,Rect(0,0,bounds.width,bounds.height))
							.drawFunc_({
								Pen.width_(lineWidth);
								slices_fa.do{
									arg arr;
									var start = arr[0].linlin(0,audio_buf.numFrames,0,bounds.width);
									var end = arr[1].linlin(0,audio_buf.numFrames,0,bounds.width);
									Pen.addRect(Rect(start,0,end-start,bounds.height));
									Pen.color_(categoryColors[categoryCounter].alpha_(0.25));
									Pen.fill;
								};
								categoryCounter = categoryCounter + 1;
							});
						});
					},{
						"FluidWaveform - indices_buf has neither 1 nor 2 channels. Not sure what to do with this.".warn;
					}
				);
			});

			if(feature_buf.notNil,{
				var stacked_height = bounds.height / feature_buf.numChannels;
				feature_buf.loadToFloatArray(action:{
					arg fa;
					fa = fa.clump(feature_buf.numChannels).flop;
					fa.do({
						arg channel, channel_i;
						var maxy;// a lower value;
						var miny; // a higher value;

						if(stackFeatures,{
							miny = stacked_height * (channel_i + 1);
							maxy = stacked_height * channel_i;
						},{
							miny = bounds.height;
							maxy = 0;
						});

						channel = channel.resamp1(bounds.width).linlin(channel.minItem,channel.maxItem,miny,maxy);
						UserView(win,Rect(0,0,bounds.width,bounds.height))
						.drawFunc_({
							Pen.moveTo(Point(0,channel[0]));
							channel[1..].do{
								arg val, i;
								Pen.lineTo(Point(i+1,val));
							};
							Pen.color_(categoryColors[categoryCounter]);
							categoryCounter = categoryCounter + 1;
							Pen.stroke;
						});
					});
				})
			});

			win.front;
		}.play(AppClock);
	}
}

