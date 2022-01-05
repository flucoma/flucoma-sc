// FluidWaveform

(
s.waitForBoot{
	// b = Buffer.read(s,FluidFilesPath("Tremblay-AaS-AcousticStrums-M.wav"));
	b = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	m = Buffer(s);

	// https://colorcet.com/
	~csv_colors = CSVFileReader.readInterpret(PathName(thisProcess.nowExecutingPath).pathOnly+/+"CETperceptual_csv_0_1/CET-L16.csv");

	~colors = ~csv_colors.collect{
		arg row;
		Color.fromArray(row);
	};

	s.sync;
	FluidBufSTFT.processBlocking(s,b,magnitude:m);
	s.sync;

	m.loadToFloatArray(action:{
		arg mags;

		// mags.clump(m.numChannels)[105].plot;

		mags = (mags / mags.maxItem).ampdb.linlin(-120.0,0.0,0,255).asInteger;

		// [0,1,2].do{
		// 	arg amt;
		// 	"# of %s in mags: %".format(amt,mags.count({arg val; val == amt})).postln;
		// };

		~img = Image(m.numFrames,m.numChannels);

		mags.do{
			arg mag, index;
			~img.setColor(~colors[mag], index.div(m.numChannels), m.numChannels - 1 - index.mod(m.numChannels));
		};

		// ~img.plot;

		w = Window("est",Rect(0,0,1200,300));

		u = UserView(w,w.bounds)
		.drawFunc_{
			~img.drawInRect(u.bounds);
		};

		w.front;
	});
};
)