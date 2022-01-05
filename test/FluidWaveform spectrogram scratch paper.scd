// FluidWaveform

(
s.waitForBoot{
	b = Buffer.read(s,FluidFilesPath("Tremblay-AaS-AcousticStrums-M.wav"));
	m = Buffer(s);

	~csv_colors = CSVFileReader.readInterpret("/Users/macprocomputer/Desktop/_flucoma/CETperceptual_csv_0_1/CET-L08.csv");

	// ~csv_colors.shape.postln;
	// ~csv_colors[0].postln;

	~colors = ~csv_colors.collect{
		arg row;
		Color.fromArray(row);
	};

	// ~colors[0].postln;

	s.sync;
	FluidBufSTFT.processBlocking(s,b,magnitude:m);
	s.sync;
	w = Window("spectrogram test",Rect(0,0,1600,900));
	w.background_(Color.white);

	m.loadToFloatArray(action:{
		arg mags;
		// var maxItem;
		// var minItem;

		mags = mags / mags.maxItem;

		mags = mags.log10 * 20;

		"one thing: %".format(mags[100]).postln;

		mags = mags.clump(m.numChannels);

		mags = mags.collect{
			arg frame;
			frame.resamp1(w.bounds.height);
		};

		mags = mags.flop.collect{
			arg row;
			row.resamp1(w.bounds.width);
		}.flop;

		mags.shape.postln;

		//maxItem = mags.collect(_.maxItem).maxItem;
		// minItem = mags.collect(_.minItem).minItem;

		// maxItem.postln;

		mags = mags.collect{
			arg frame;
			frame.collect{
				arg mag;
				mag.linlin(-120.0,0.0,0.0,255.0).asInteger;
			};
		};

		100.do{
			// mags[rrand(0,mags.size-1)].choose.postln;
		};

		~uv = UserView(w,w.bounds)
		.drawFunc_{
			mags.do{
				arg frame, x;
				frame.do{
					arg mag, y;
					// mag.postln;
					Pen.addRect(Rect(x,w.bounds.height - y,1,1));
					Pen.fillColor_(~colors[mag]);
					Pen.fill;
				}
			}
		};

		w.front;
	});
};
)