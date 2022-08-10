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

	*categoryColors {
		^FluidViewer.createCatColors;
	}
}

FluidWaveformAudioLayer {
	var audioBuffer, waveformColor;

	*new {
		arg audioBuffer, waveformColor(Color.gray);
		^super.newCopyArgs(audioBuffer, waveformColor);
	}

	draw {
		var path = "%%_%_FluidWaveform.wav".format(PathName.tmp,Date.localtime.stamp,UniqueID.next);
		var sfv = SoundFileView();
		sfv.peakColor_(waveformColor);
		sfv.drawsBoundingLines_(false);
		sfv.rmsColor_(Color.clear);
		sfv.background_(Color.clear);
		sfv.gridOn_(false);

		forkIfNeeded({
			audioBuffer.write(path,"wav");
			audioBuffer.server.sync;
			sfv.readFile(SoundFile(path));
			File.delete(path)
		}, AppClock);

		^sfv
	}
}

FluidWaveformIndicesLayer : FluidViewer {
	var indicesBuffer, audioBuffer, color, lineWidth;

	*new {
		arg indicesBuffer, audioBuffer, color(Color.red), lineWidth = 1;
		^super.newCopyArgs(indicesBuffer, audioBuffer, color, lineWidth);
	}

	draw {
		var userView;
		var condition = Condition();
		var slices_fa = nil;

		var numChannels = indicesBuffer.numChannels;
		if ([1, 2].includes(numChannels).not) {
			Error(
				"% indicesBuffer must have either 1 or 2 channels."
				.format(this.class)
			).throw;
		};
		if (audioBuffer.isNil) {
			Error(
				"% In order to display an indicesBuffer an audioBuffer must be included."
				.format(this.class)
			).throw;
		};

		userView = UserView();

		forkIfNeeded({
			indicesBuffer.loadToFloatArray(action: {
				arg v;
				slices_fa = v;
				condition.test = true;
				condition.signal;
			});
			condition.wait;

			userView.drawFunc = numChannels.switch(
				1, {{
					arg viewport;
					var bounds = viewport.bounds;
					Pen.width_(lineWidth);
					slices_fa.do{
						arg start_samp;
						var x = start_samp.linlin(0,audioBuffer.numFrames,0,bounds.width);
						Pen.line(Point(x,0),Point(x,bounds.height));
						Pen.color_(color);
						Pen.stroke;
				}};
				},
				2, {{
					arg viewport;
					var bounds = viewport.bounds;
					Pen.width_(lineWidth);
					slices_fa.clump(2).do{
						arg arr;
						var start = arr[0].linlin(0,audioBuffer.numFrames,0,bounds.width);
						var end = arr[1].linlin(0,audioBuffer.numFrames,0,bounds.width);
						Pen.addRect(Rect(start,0,end-start,bounds.height));
						Pen.color_(color.alpha_(0.25));
						Pen.fill;
				}};
				}
			);
		}, AppClock);
		^userView;
	}
}

FluidWaveformFeaturesLayer : FluidViewer {
	var featuresBuffer, colors, stackFeatures, normalizeFeaturesIndependently, lineWidth;

	*new {
		arg featuresBuffer, colors, stackFeatures = false, normalizeFeaturesIndependently = true, lineWidth = 1;
		colors = colors ?? { this.createCatColors };
		// we'll index into it to draw, so just in case the user passed just one color, this will ensure it can be "indexed" into
		if (colors.isKindOf(SequenceableCollection).not) { colors = [colors] };
		^super.newCopyArgs(
			featuresBuffer,colors,stackFeatures,normalizeFeaturesIndependently, lineWidth
		);
	}

	draw {
		var userView = UserView();
		var condition = Condition();
		var fa = nil;

		forkIfNeeded({
			var minVal = 0, maxVal = 0;

			featuresBuffer.loadToFloatArray(action:{
				arg v;
				fa = v;
				condition.test = true;
				condition.signal;
			});
			condition.wait;

			if(normalizeFeaturesIndependently.not,{
				minVal = fa.minItem;
				maxVal = fa.maxItem;
			});

			fa = fa.clump(featuresBuffer.numChannels).flop;

			userView.drawFunc_({
				arg viewport;
				var bounds = viewport.bounds;
				var stacked_height;
				if (stackFeatures) {
					stacked_height = bounds.height / featuresBuffer.numChannels;
				};

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

					channel = channel.resamp1(bounds.width)
					.linlin(minVal,maxVal,miny,maxy);

					Pen.width = lineWidth;
					Pen.moveTo(Point(0,channel[0]));
					channel[1..].do{
						arg val, i;
						Pen.lineTo(Point(i+1,val));
					};
					Pen.color_(colors[channel_i % colors.size]);
					Pen.stroke;
				});
			});
		}, AppClock);

		^userView;
	}
}

FluidWaveformImageLayer {
	var imageBuffer, imageColorScheme, imageColorScaling, imageAlpha;

	*new {
		arg imageBuffer, imageColorScheme = 0, imageColorScaling = 0, imageAlpha = 1;
		^super.newCopyArgs(
			imageBuffer, imageColorScheme, imageColorScaling, imageAlpha
		);
	}

	draw {
		var colors = this.prGetColorsFromScheme(imageColorScheme);
		var condition = Condition();
		var vals = nil;
		var userView = UserView();

		forkIfNeeded({
			var img = Image(imageBuffer.numFrames, imageBuffer.numChannels);
			imageBuffer.loadToFloatArray(action: {
				arg v;
				vals = v;
				condition.test = true;
				condition.signal;
			});
			condition.wait;

			imageColorScaling.switch(
				FluidWaveform.lin,{
					var minItem = vals.minItem;
					vals = (vals - minItem) / (vals.maxItem - minItem);
					vals = (vals * 255).asInteger;
				},
				FluidWaveform.log,{
					vals = (vals + 1e-6).log;
					vals = vals.linlin(vals.minItem,vals.maxItem,0.0,255.0).asInteger;
				},
				{
					"% colorScaling argument % is invalid.".format(thisMethod,imageColorScaling).warn;
				}
			);

			vals.do{
				arg val, index;
				img.setColor(colors[val], index.div(imageBuffer.numChannels), imageBuffer.numChannels - 1 - index.mod(imageBuffer.numChannels));
			};

			userView.drawFunc = {
				arg viewport;
				var bounds = viewport.bounds;
				img.drawInRect(
					Rect(0, 0, bounds.width, bounds.height),
					fraction: imageAlpha
				);
			};
		}, AppClock);
		^userView;
	}

	loadColorFile {
		arg filename;
		^CSVFileReader.readInterpret(FluidFilesPath("../color-schemes/%.csv".format(filename))).collect{
			arg row;
			Color.fromArray(row);
		}
	}

	prGetColorsFromScheme {
		arg imageColorScheme;
		var colors;
		if(imageColorScheme.isKindOf(Color),{
			colors = 256.collect{
				arg i;
				imageColorScheme.copy.alpha_(i.linlin(0,255,0.0,1.0));
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

		^colors;
	}
}

FluidWaveform : FluidViewer {
	classvar <lin = 0, <log = 1;
	var <parent, bounds, standalone, view, <layers;

	*new {
		arg audioBuffer, indicesBuffer, featuresBuffer,
		parent, bounds,
		lineWidth = 1, waveformColor, stackFeatures = false,
		imageBuffer, imageColorScheme = 0, imageAlpha = 1,
		normalizeFeaturesIndependently = true, imageColorScaling = 0,
		standalone = true;

		if (parent.notNil) { standalone = false };

		^super.newCopyArgs(parent, bounds, standalone)
		.init(audioBuffer, indicesBuffer, featuresBuffer,
			lineWidth, waveformColor, stackFeatures,
			imageBuffer, imageColorScheme, imageAlpha,
			normalizeFeaturesIndependently, imageColorScaling
		);
	}

	init {
		arg audio_buf, slices_buf, feature_buf,
		lineWidth = 1, waveformColor, stackFeatures = false,
		imageBuffer, imageColorScheme = 0, imageAlpha = 1,
		normalizeFeaturesIndependently = true, imageColorScaling = 0;
		var plotImmediately = false;

		layers = List.new;
		waveformColor = waveformColor ? Color(*0.6.dup(3));

		if (imageBuffer.notNil) {
			this.addImageLayer(imageBuffer, imageColorScheme, imageColorScaling, imageAlpha);
			if(standalone && bounds.isNil) {
				bounds = Rect(0,0,imageBuffer.numFrames,imageBuffer.numChannels);
			};
			plotImmediately = true;
		};

		if (audio_buf.notNil) {
			this.addAudioLayer(audio_buf, waveformColor);
			plotImmediately = true;
		};

		if (feature_buf.notNil) {
			this.addFeaturesLayer(feature_buf, this.createCatColors, stackFeatures, normalizeFeaturesIndependently, lineWidth);
			plotImmediately = true;
		};

		if (slices_buf.notNil) {
			this.addIndicesLayer(slices_buf, audio_buf, Color.red, lineWidth);
			plotImmediately = true;
		};

		if (plotImmediately) { this.front };
	}

	addImageLayer {
		arg imageBuffer, imageColorScheme = 0, imageColorScaling = 0, imageAlpha = 1;
		var l = FluidWaveformImageLayer(imageBuffer, imageColorScheme, imageColorScaling, imageAlpha);
		layers.add(l);
	}

	addAudioLayer {
		arg audioBuffer, waveformColor;
		var l = FluidWaveformAudioLayer(audioBuffer, waveformColor);
		layers.add(l);
	}

	addIndicesLayer {
		arg indicesBuffer, audioBuffer, color, lineWidth = 1;
		var l = FluidWaveformIndicesLayer(indicesBuffer,audioBuffer,color,lineWidth);
		layers.add(l);
	}

	addFeaturesLayer {
		arg featuresBuffer, colors, stackFeatures = false, normalizeFeaturesIndependently = true, lineWidth = 1;
		var l = FluidWaveformFeaturesLayer(featuresBuffer,colors,stackFeatures,normalizeFeaturesIndependently, lineWidth);
		layers.add(l);
	}

	addLayer {
		arg fluidWaveformLayer;
		layers.add(fluidWaveformLayer);
	}

	front {
		this.prMakeView;
		fork({
			this.refresh;
			if (standalone) { parent.front };
		}, AppClock);
	}

	// defer({}, nil) forks if not already running on the AppClock
	refresh {
		forkIfNeeded({
			var noView = if (view.isNil) { true } { view.isClosed };
			if (noView) { this.prMakeView };
			view.removeAll;
			view.layout = StackLayout().mode_(\stackAll);
			layers.do {
				arg layer, n;
				var layerView;
				layerView = layer.draw;
				view.layout.add(layerView);
				view.layout.index = view.layout.index + 1;
			};
			view.refresh;
		}, AppClock);
	}

	close {
		parent.close;
	}

	prMakeView {
		if (parent.isNil) {
			if (standalone) {
				parent = Window("FluidWaveform", bounds: bounds ? Rect(0,0,800,200));
				parent.background_(Color.white);
				view = parent.view;
			} {
				parent = view = View();
				view.background_(Color.white);
			}
		} {
			view = View(parent, bounds);
			view.background_(Color.white);
		};
	}

	asView { ^view }
}
