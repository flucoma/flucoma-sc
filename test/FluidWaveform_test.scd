(
// og test
s.waitForBoot{

	Task{
		var buf = Buffer.read(s,"/Users/macprocomputer/Desktop/_flucoma/code/flucoma-core-src/AudioFiles/Nicol-LoopE-M.wav");
		var slicepoints = Buffer(s);

		FluidBufAmpSlice.process(s,buf,indices:slicepoints,fastRampUp:10,fastRampDown:2205,slowRampUp:4410,slowRampDown:4410,onThreshold:10,offThreshold:5,floor:-40,minSliceLength:4410,highPassFreq:20,action:{
			FluidWaveform(buf,slicepoints,bounds:Rect(0,0,1600,400)).front;
		});

	}.play(AppClock);
}
)

(
// lots of defaults
s.waitForBoot{
	Window.closeAll;

	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));

	s.sync;

	~mels = Buffer(s);
	FluidBufMelBands.processBlocking(s,~audio,features:~mels);

	~indices = Buffer(s);
	FluidBufOnsetSlice.processBlocking(s,~audio,metric:9,indices:~indices);

	~pitch_analysis = Buffer(s);
	FluidBufPitch.processBlocking(s,~audio,features:~pitch_analysis);

	s.sync;
	~fw = FluidWaveform(bounds:Rect(100,100,1200,300));
	~fw.addImageLayer(~mels,1,imageColorScaling:1);
	~fw.addAudioLayer(~audio);
	~fw.addIndicesLayer(~indices,~audio);
	~fw.addFeaturesLayer(~pitch_analysis);
	~fw.front;
}
)

(
// some more arguments
s.waitForBoot{
	Window.closeAll;

	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));

	s.sync;

	~mels = Buffer(s);
	FluidBufMelBands.processBlocking(s,~audio,features:~mels);

	~indices = Buffer(s);
	FluidBufOnsetSlice.processBlocking(s,~audio,metric:9,indices:~indices);

	~pitch_analysis = Buffer(s);
	FluidBufPitch.processBlocking(s,~audio,features:~pitch_analysis);

	~fw = FluidWaveform(bounds:Rect(100,100,1200,300));

	~fw.addImageLayer(~mels,1,1);

	~fw.addAudioLayer(~audio,Color.red);

	~fw.addIndicesLayer(~indices,~audio,Color.yellow);

	~fw.addFeaturesLayer(~pitch_analysis,Color.cyan,true);
	~fw.front;
}
)

(
// another og test
s.waitForBoot{
	Window.closeAll;

	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));

	s.sync;

	~mels = Buffer(s);
	FluidBufMelBands.processBlocking(s,~audio,features:~mels);

	~indices = Buffer(s);
	FluidBufOnsetSlice.processBlocking(s,~audio,metric:9,indices:~indices);

	~pitch_analysis = Buffer(s);
	FluidBufPitch.processBlocking(s,~audio,features:~pitch_analysis);

	s.sync;

	~fw = FluidWaveform(~audio,~indices,~pitch_analysis,imageBuffer:~mels,bounds:Rect(100,100,1200,300),imageColorScaling:1,imageColorScheme:1).front;
}
)

(
// another og test with gate info
s.waitForBoot{
	Window.closeAll;

	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));

	s.sync;

	~indices = Buffer(s);
	FluidBufAmpGate.processBlocking(s,~audio,indices:~indices,onThreshold:-20,offThreshold:-30,minSliceLength:4410,minSilenceLength:441);

	~pitch_analysis = Buffer(s);
	FluidBufPitch.processBlocking(s,~audio,features:~pitch_analysis);

	s.sync;

	~fw = FluidWaveform(~audio,~indices,~pitch_analysis,bounds:Rect(100,100,1200,300),imageColorScaling:1,imageColorScheme:1).front;
}
)


(
s.waitForBoot{
	// do some analysis
	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	~mels = Buffer(s);
	FluidBufMelBands.processBlocking(s,~audio,features:~mels);
	~indices = Buffer(s);
	FluidBufOnsetSlice.processBlocking(s,~audio,metric:9,indices:~indices);
	~pitch_analysis = Buffer(s);
	FluidBufPitch.processBlocking(s,~audio,features:~pitch_analysis);
	s.sync;
	"analysis done".postln;
}
)

(
// plot it all
~fw = FluidWaveform(bounds:Rect(100,100,1200,300));
~fw.addImageLayer(~mels,2,imageColorScaling:1);
~fw.addAudioLayer(~audio,Color(1,1,1,0.5));
~fw.addIndicesLayer(~indices,~audio,Color.black);
~fw.addFeaturesLayer(~pitch_analysis,[Color.cyan,Color.magenta]);
~fw.front;
)

(
// plot most of it, but then...
~fw = FluidWaveform(bounds:Rect(100,100,1200,300));
~fw.addImageLayer(~mels,2,imageColorScaling:1);
~fw.addAudioLayer(~audio,Color(1,1,1,0.5));
~fw.addIndicesLayer(~indices,~audio,Color.black);
~fw.front;
)

// add one more
~fw.addFeaturesLayer(~pitch_analysis,[Color.cyan,Color.yellow]).front;

// check how many layers
~fw.layers

(
// plot differen components with different colors
s.waitForBoot{
	// ~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	~audio = Buffer.read(s,FluidFilesPath("Tremblay-ASWINE-ScratchySynth-M.wav"));
	~resynth = Buffer(s);
	~n_components = 2;
	~resynths = {Buffer(s)} ! ~n_components;
	~mels = {Buffer(s)} ! ~n_components;
	~activations = Buffer(s);
	s.sync;
	FluidBufNMF.processBlocking(s,~audio,resynth:~resynth,activations:~activations,components:~n_components);
	~n_components.do{
		arg i;
		FluidBufCompose.processBlocking(s,~resynth,startChan:i,numChans:1,destination:~resynths[i]);
		FluidBufMelBands.processBlocking(s,~resynths[i],features:~mels[i]);
	};

	~colors = FluidViewer.createCatColors.collect{
		arg color;
		color.alpha_(0.4);
		color;
	};

	s.sync;

	~resynths.postln;
	~fw = FluidWaveform(bounds:Rect(0,0,1600,400));

	~n_components.do{
		arg i;
		~fw.addAudioLayer(~resynths[i],~colors[i]);
	};

	// ~n_components.do{
	// 	arg i;
	// 	~fw.addImageLayer(~mels[i],~colors[i],1,1);
	// };

	~fw.front;
}
)
