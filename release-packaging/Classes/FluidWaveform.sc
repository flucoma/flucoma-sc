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
		arg audioBuffer, slicesBuffer, featureBuffer, bounds, lineWidth = 1, waveformColor, stackFeatures = false;
		^super.new.init(audioBuffer,slicesBuffer, featureBuffer, bounds, lineWidth, waveformColor,stackFeatures);
	}

	init {
		arg audio_buf, slices_buf, feature_buf, bounds, lineWidth, waveformColor,stackFeatures = false;
		Task{
			var sfv, categoryCounter = 0;

			waveformColor = waveformColor ? Color(*0.5.dup(3));

			this.createCatColors;

			bounds = bounds ? Rect(0,0,800,200);
			win = Window("FluidWaveform",bounds);
			win.background_(Color.white);

			if(audio_buf.notNil,{
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

