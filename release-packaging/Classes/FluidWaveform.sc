FluidViewer {

	createCatColors {
		^FluidViewer.createCatColors;
	}

	*createCatColors {
		// colors from: https://github.com/d3/d3-scale-chromatic/blob/main/src/categorical/category10.js
		^"1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf".clump(6).collect{
			arg six;
			Color(*six.clump(2).collect{
				arg two;
				"0x%".format(two).interpret / 255;
			});
		}
	}
}

FluidWaveformAudioLayer {
	var audioBuffer, waveformColor;

	*new {
		arg audioBuffer, waveformColor;
		^super.new.init(audioBuffer,waveformColor);
	}

	init {
		arg audioBuffer_, waveformColor_;

		audioBuffer = audioBuffer_;
		waveformColor = waveformColor_ ? Color.gray;
	}

	draw {
		arg win, bounds;
		fork({
			var path = "%%_%_FluidWaveform.wav".format(PathName.tmp,Date.localtime.stamp,UniqueID.next);
			var sfv;

			audioBuffer.write(path,"wav");

			audioBuffer.server.sync;

			sfv = SoundFileView(win,bounds);
			sfv.peakColor_(waveformColor);
			sfv.drawsBoundingLines_(false);
			sfv.rmsColor_(Color.clear);
			sfv.background_(Color.clear);
			sfv.readFile(SoundFile(path));
			sfv.gridOn_(false);

			File.delete(path);
		},AppClock);
		^audioBuffer.server;
	}
}

FluidWaveformIndicesLayer : FluidViewer {
	var indicesBuffer, audioBuffer, color, lineWidth;

	*new {
		arg indicesBuffer, audioBuffer, color, lineWidth = 1;
		^super.new.init(indicesBuffer, audioBuffer, color, lineWidth);
	}

	init {
		arg indicesBuffer_, audioBuffer_, color_, lineWidth_;
		indicesBuffer = indicesBuffer_;
		audioBuffer = audioBuffer_;
		color = color_ ? Color.red;
		lineWidth = lineWidth_;
	}

	draw {
		arg win, bounds;

		if(audioBuffer.notNil,{
			fork({
				indicesBuffer.numChannels.switch(
					1,{
						indicesBuffer.loadToFloatArray(action:{
							arg slices_fa;
							UserView(win,bounds)
							.drawFunc_({
								Pen.width_(lineWidth);
								slices_fa.do{
									arg start_samp;
									var x = start_samp.linlin(0,audioBuffer.numFrames,0,bounds.width);
									Pen.line(Point(x,0),Point(x,bounds.height));
									Pen.color_(color);
									Pen.stroke;
								};
							});
						});
					},
					2,{
						indicesBuffer.loadToFloatArray(action:{
							arg slices_fa;
							slices_fa = slices_fa.clump(2);
							UserView(win,bounds)
							.drawFunc_({
								Pen.width_(lineWidth);
								slices_fa.do{
									arg arr;
									var start = arr[0].linlin(0,audioBuffer.numFrames,0,bounds.width);
									var end = arr[1].linlin(0,audioBuffer.numFrames,0,bounds.width);
									Pen.addRect(Rect(start,0,end-start,bounds.height));
									Pen.color_(color.alpha_(0.25));
									Pen.fill;
								};

							});
						});
					},{
						Error("% indicesBuffer must have either 1 nor 2 channels.".format(this.class)).throw;
					}
				);
			},AppClock);
			^indicesBuffer.server;
		},{
			Error("% In order to display an indicesBuffer an audioBuffer must be included.".format(this.class)).throw;
		});
	}
}

FluidWaveformFeaturesLayer : FluidViewer {
	var featuresBuffer, colors, stackFeatures, normalizeFeaturesIndependently;

	*new {
		arg featuresBuffer, colors, stackFeatures = false, normalizeFeaturesIndependently = true;
		^super.new.init(featuresBuffer,colors,stackFeatures,normalizeFeaturesIndependently);
	}

	init {
		arg featuresBuffer_, colors_, stackFeatures_ = false, normalizeFeaturesIndependently_ = true;
		featuresBuffer = featuresBuffer_;
		normalizeFeaturesIndependently = normalizeFeaturesIndependently_;
		stackFeatures = stackFeatures_;
		colors = colors_ ?? {this.createCatColors};

		// we'll index into it to draw, so just in case the user passed just one color, this will ensure it can be "indexed" into
		if(colors.isKindOf(SequenceableCollection).not,{colors = [colors]});
	}

	draw {
		arg win, bounds;

		featuresBuffer.loadToFloatArray(action:{
			arg fa;
			var minVal = 0, maxVal = 0;
			var stacked_height;

			if(stackFeatures,{
				stacked_height = bounds.height / featuresBuffer.numChannels;
			});

			if(normalizeFeaturesIndependently.not,{
				minVal = fa.minItem;
				maxVal = fa.maxItem;
			});

			fa = fa.clump(featuresBuffer.numChannels).flop;

			fork({
				fa.do({
					arg channel, channel_i;
					var maxy;// a lower value;
					var miny;// a higher value;

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

					UserView(win,bounds)
					.drawFunc_({
						Pen.moveTo(Point(0,channel[0]));
						channel[1..].do{
							arg val, i;
							Pen.lineTo(Point(i+1,val));
						};
						Pen.color_(colors[channel_i % colors.size]);
						Pen.stroke;
					});
				});
			},AppClock);
		});
		^featuresBuffer.server;
	}
}

FluidWaveformImageLayer {
	var imageBuffer, imageColorScheme, imageColorScaling, imageAlpha;

	*new {
		arg imageBuffer, imageColorScheme = 0, imageColorScaling = 0, imageAlpha = 1;
		^super.new.init(imageBuffer,imageColorScheme,imageColorScaling,imageAlpha);
	}

	init {
		arg imageBuffer_, imageColorScheme_ = 0, imageColorScaling_ = 0, imageAlpha_ = 1;

		imageBuffer = imageBuffer_;
		imageColorScheme = imageColorScheme_;
		imageColorScaling = imageColorScaling_;
		imageAlpha = imageAlpha_;
	}

	draw {
		arg win, bounds;
		var colors;

		if(imageColorScheme.isKindOf(Color),{
			// "imageColorScheme is a kind of Color".postln;
			colors = 256.collect{
				arg i;
				Color(imageColorScheme.red,imageColorScheme.green,imageColorScheme.blue,i.linlin(0,255,0.0,1.0));
			};
		},{
			imageColorScheme.switch(
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
					"% imageColorScheme: % is not valid.".format(thisMethod,imageColorScheme).warn;
				}
			);
		});

		imageBuffer.loadToFloatArray(action:{
			arg vals;
			fork({
				var img = Image(imageBuffer.numFrames,imageBuffer.numChannels);

				imageColorScaling.switch(
					FluidWaveform.lin,{
						var minItem = vals.minItem;
						vals = (vals - minItem) / (vals.maxItem - minItem);
						vals = (vals * 255).asInteger;
					},
					FluidWaveform.log,{
						vals = (vals + 1e-6).log;
						vals = vals.linlin(vals.minItem,vals.maxItem,0.0,255.0).asInteger;
						// vals.postln;
					},
					{
						"% colorScaling argument % is invalid.".format(thisMethod,imageColorScaling).warn;
					}
				);

				// colors.postln;

				vals.do{
					arg val, index;
					img.setColor(colors[val], index.div(imageBuffer.numChannels), imageBuffer.numChannels - 1 - index.mod(imageBuffer.numChannels));
				};

				UserView(win,bounds)
				.drawFunc_{
					img.drawInRect(Rect(0,0,bounds.width,bounds.height),fraction:imageAlpha);
				};
			},AppClock);
		});
		^imageBuffer.server;
	}

	loadColorFile {
		arg filename;
		^CSVFileReader.readInterpret(FluidFilesPath("../Resources/color-schemes/%.csv".format(filename))).collect{
			arg row;
			Color.fromArray(row);
		}
	}
}

FluidWaveform : FluidViewer {
	classvar <lin = 0, <log = 1;
	var <win, bounds, display_bounds, <layers;

	*new {
		arg audioBuffer, indicesBuffer, featuresBuffer, parent, bounds, lineWidth = 1, waveformColor, stackFeatures = false, imageBuffer, imageColorScheme = 0, imageAlpha = 1, normalizeFeaturesIndependently = true, imageColorScaling = 0;
		^super.new.init(audioBuffer,indicesBuffer, featuresBuffer, parent, bounds, lineWidth, waveformColor,stackFeatures,imageBuffer,imageColorScheme,imageAlpha,normalizeFeaturesIndependently,imageColorScaling);
	}

	init {
		arg audio_buf, slices_buf, feature_buf, parent_, bounds_, lineWidth = 1, waveformColor,stackFeatures = false, imageBuffer, imageColorScheme = 0, imageAlpha = 1, normalizeFeaturesIndependently = true, imageColorScaling = 0;
		layers = List.new;

		fork({
			var plotImmediately = false;

			bounds = bounds_;

			waveformColor = waveformColor ? Color(*0.6.dup(3));

			if(bounds.isNil && imageBuffer.notNil,{
				bounds = Rect(0,0,imageBuffer.numFrames,imageBuffer.numChannels);
			});

			bounds = bounds ? Rect(0,0,800,200);

			if(parent_.isNil,{
				win = Window("FluidWaveform",bounds);
				win.background_(Color.white);
				display_bounds = Rect(0,0,bounds.width,bounds.height);
			},{
				win = parent_;
				display_bounds = bounds;
			});

			if(imageBuffer.notNil,{
				this.addImageLayer(imageBuffer,imageColorScheme,imageColorScaling,imageAlpha);
				imageBuffer.server.sync;
				plotImmediately = true;
			});

			if(audio_buf.notNil,{
				this.addAudioLayer(audio_buf,waveformColor);
				audio_buf.server.sync;
				plotImmediately = true;
			});

			if(feature_buf.notNil,{
				this.addFeaturesLayer(feature_buf,this.createCatColors,stackFeatures,normalizeFeaturesIndependently);
				feature_buf.server.sync;
				plotImmediately = true;
			});

			if(slices_buf.notNil,{
				this.addIndicesLayer(slices_buf,audio_buf,Color.red,lineWidth);
				slices_buf.server.sync;
				plotImmediately = true;
			});

			if(plotImmediately,{this.front;});
		},AppClock);
	}

	addImageLayer {
		arg imageBuffer, imageColorScheme = 0, imageColorScaling = 0, imageAlpha = 1;
		var l = FluidWaveformImageLayer(imageBuffer,imageColorScheme,imageColorScaling,imageAlpha);

		// l.postln;
		layers.add(l);
		// layers.postln;
		// l.draw(win,display_bounds);
	}

	addAudioLayer {
		arg audioBuffer, waveformColor;
		var l = FluidWaveformAudioLayer(audioBuffer,waveformColor);

		// l.postln;
		layers.add(l);
		// layers.postln;

		// l.draw(win,display_bounds);
	}

	addIndicesLayer {
		arg indicesBuffer, audioBuffer, color, lineWidth = 1;
		var l = FluidWaveformIndicesLayer(indicesBuffer,audioBuffer,color,lineWidth);

		// l.postln;
		layers.add(l);
		// layers.postln;

		// l.draw(win,display_bounds);
	}

	addFeaturesLayer {
		arg featuresBuffer, colors, stackFeatures = false, normalizeFeaturesIndependently = true;
		var l = FluidWaveformFeaturesLayer(featuresBuffer,colors,stackFeatures,normalizeFeaturesIndependently);

		// l.postln;
		layers.add(l);
		// layers.postln;

		// l.draw(win,display_bounds);
	}

	addLayer {
		arg fluidWaveformLayer;
		layers.add(fluidWaveformLayer);
	}

	front {
		fork({

			UserView(win,display_bounds)
			.drawFunc_{
				Pen.fillColor_(Color.white);
				Pen.addRect(Rect(0,0,bounds.width,bounds.height));
				Pen.fill;
			};

			layers.do{
				arg layer;
				// layer.postln;
				layer.draw(win,display_bounds).sync;
			};
			win.front;
		},AppClock);
	}

	close {
		win.close;
	}
}

