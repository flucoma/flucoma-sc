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
	classvar lin = 0, log = 1;
	var <win;

	*new {
		arg audioBuffer, indicesBuffer, featureBuffer, parent, bounds, lineWidth = 1, waveformColor, stackFeatures = false, rasterBuffer, rasterColorScheme = 0, rasterAlpha = 1, normalizeFeaturesIndependently = true, scaling = 1;
		^super.new.init(audioBuffer,indicesBuffer, featureBuffer, parent, bounds, lineWidth, waveformColor,stackFeatures,rasterBuffer,rasterColorScheme,rasterAlpha,normalizeFeaturesIndependently,scaling);
	}

	close {
		win.close;
	}

	loadColorFile {
		arg filename;
		^CSVFileReader.readInterpret(FluidFilesPath("../Resources/color-schemes/%.csv".format(filename))).collect{
			arg row;
			Color.fromArray(row);
		}
	}

	init {
		arg audio_buf, slices_buf, feature_buf, parent_, bounds, lineWidth, waveformColor,stackFeatures = false, rasterBuffer, rasterColorScheme = 0, rasterAlpha = 1, normalizeFeaturesIndependently = true, scaling = 1;
		Task{
			var sfv, categoryCounter = 0, xpos, ypos;

			waveformColor = waveformColor ? Color(*0.6.dup(3));

			this.createCatColors;

			if(bounds.isNil && rasterBuffer.notNil,{
				bounds = Rect(0,0,rasterBuffer.numFrames,rasterBuffer.numChannels);
			});

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

			if(rasterBuffer.notNil,{
				var condition = Condition.new;
				var colors;

				// TODO: no need for this to be a switch statement.
				rasterColorScheme.switch(
					0,{
						colors = this.loadColorFile("CET-L02");
					},
					1,{
						colors = this.loadColorFile("CET-L16");
					},
					2,{
						colors = this.loadColorFile("CET-L08");
					},
					3,{
						colors = this.loadColorFile("CET-L03");
					},
					4,{
						colors = this.loadColorFile("CET-L04");
					},
					{
						"% spectrogramColorScheme: % is not valid.".format(thisMethod,rasterColorScheme).warn;
					}
				);

				rasterBuffer.loadToFloatArray(action:{
					arg vals;
					fork({
						var img = Image(rasterBuffer.numFrames,rasterBuffer.numChannels);

						scaling.switch(
							FluidWaveform.lin,{
								var minItem = vals.minItem;
								vals = (vals - minItem) / (vals.maxItem - minItem);
								vals = (vals * 255).asInteger;
							},
							FluidWaveform.log,{
								vals = (vals + 1e-6).log;
								vals = vals.linlin(0.0,vals.maxItem,0.0,255.0).asInteger;
							},
							{
								"% scaling argument % is invalid.".format(thisMethod,scaling).warn;
							}
						);

						vals.do{
							arg val, index;
							img.setColor(colors[val], index.div(rasterBuffer.numChannels), rasterBuffer.numChannels - 1 - index.mod(rasterBuffer.numChannels));
						};

						UserView(win,Rect(xpos,ypos,bounds.width,bounds.height))
						.drawFunc_{
							img.drawInRect(Rect(0,0,bounds.width,bounds.height),fraction:rasterAlpha);
						};

						condition.unhang;
					},AppClock)
				});
				condition.hang;
			});

			if(audio_buf.notNil,{
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

