FluidViewer {
	var categoryColors;

	createCatColors {
		// colors from: https://github.com/d3/d3-scale-chromatic/blob/main/src/categorical/category10.js
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
		arg audioBuffer, slicesBuffer, featureBuffer, parent, bounds, lineWidth = 1, waveformColor, stackFeatures = false, showSpectrogram = false, spectrogramColorScheme = 0, spectrogramAlpha = 1, showWaveform = true, normalizeFeaturesIndependently = true;
		^super.new.init(audioBuffer,slicesBuffer, featureBuffer, parent, bounds, lineWidth, waveformColor,stackFeatures,showSpectrogram,spectrogramColorScheme,spectrogramAlpha,showWaveform,normalizeFeaturesIndependently);
	}

	close {
		win.close;
	}

	init {
		arg audio_buf, slices_buf, feature_buf, parent_, bounds, lineWidth, waveformColor,stackFeatures = false, showSpectrogram = false, spectrogramColorScheme = 0, spectrogramAlpha = 1, showWaveform = true,normalizeFeaturesIndependently = true;
		Task{
			var sfv, categoryCounter = 0, xpos, ypos;

			waveformColor = waveformColor ? Color(*0.dup(3));

			this.createCatColors;

			bounds = bounds ? Rect(0,0,800,200);

			if(parent_.isNil,{
				xpos = 0;
				ypos = 0;
				win = Window("FluidWaveform",bounds);
				win.background_(Color.white);
			},{
				xpos = bounds.left;
				ypos = bounds.top;
				win = parent_;
				UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
				.drawFunc_{
					Pen.fillColor_(Color.white);
					Pen.addRect(Rect(0,0,bounds.width,bounds.height));
					Pen.fill;
				};
			});

			if(audio_buf.notNil,{
				if(showSpectrogram,{
					var magsbuf = Buffer(audio_buf.server);
					var condition = Condition.new;
					var colors;

					spectrogramColorScheme.switch(
						0,{
							colors = 256.collect{
								arg i;
								Color.gray(i / 255.0);
							};
						},
						1,{
							colors = CSVFileReader.readInterpret(FluidFilesPath("../Resources/color-schemes/CET-L16.csv")).collect{
								arg row;
								Color.fromArray(row);
							};
						},{
							"% spectrogramColorScheme: % is not valid.".format(thisMethod,spectrogramColorScheme).warn;
						}
					);

					FluidBufSTFT.processBlocking(audio_buf.server,audio_buf,magnitude:magsbuf,action:{
						magsbuf.loadToFloatArray(action:{
							arg mags;
							fork({
								var img = Image(magsbuf.numFrames,magsbuf.numChannels);
								mags = (mags / mags.maxItem).ampdb.linlin(-120.0,0.0,0,255).asInteger;

								mags.do{
									arg mag, index;
									// colors[mag].postln;
									img.setColor(colors[mag], index.div(magsbuf.numChannels), magsbuf.numChannels - 1 - index.mod(magsbuf.numChannels));
								};

								UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
								.drawFunc_{
									img.drawInRect(Rect(0,0,bounds.width,bounds.height),fraction:spectrogramAlpha);
								};

								condition.unhang;
								magsbuf.free;
							},AppClock)
						});
					});
					condition.hang;
				});

				if(showWaveform,{
					var path = "%%_%_FluidWaveform.wav".format(PathName.tmp,Date.localtime.stamp,UniqueID.next);

					audio_buf.write(path,"wav");

					audio_buf.server.sync;

					sfv = SoundFileView(win,Rect(xpos,ypos,bounds.width,bounds.height));
					sfv.peakColor_(waveformColor);
					// sfv.rmsColor_(Color.black);
					sfv.drawsBoundingLines_(false);
					sfv.rmsColor_(Color.clear);
					// sfv.background_(Color.white);
					sfv.background_(Color.clear);
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
							UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
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
							UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
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
					var minVal = 0, maxVal = 0;

					if(normalizeFeaturesIndependently.not,{
						minVal = fa.minItem;
						maxVal = fa.maxItem;
					});

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

						if(normalizeFeaturesIndependently,{
							minVal = channel.minItem;
							maxVal = channel.maxItem;
						});

						channel = channel.resamp1(bounds.width).linlin(minVal,maxVal,miny,maxy);

						UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
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

