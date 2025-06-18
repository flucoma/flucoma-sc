(
// og test
s.waitForBoot{
	var buf = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	var slicepoints = Buffer(s);

	FluidBufAmpSlice.process(s,buf,indices:slicepoints,fastRampUp:10,fastRampDown:2205,slowRampUp:4410,slowRampDown:4410,onThreshold:10,offThreshold:5,floor:-40,minSliceLength:4410,highPassFreq:20,action:{
		defer{FluidWaveform(buf,slicepoints,bounds:Rect(0,0,1600,400)).front};
	});
}
)


(
// embed it in a different window
s.waitForBoot{

	var buf = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	var slicepoints = Buffer(s);

	FluidBufAmpSlice.process(s,buf,indices:slicepoints,fastRampUp:10,fastRampDown:2205,slowRampUp:4410,slowRampDown:4410,onThreshold:10,offThreshold:5,floor:-40,minSliceLength:4410,highPassFreq:20,action:{
		defer{
			~win = Window("test",Rect(0,0,1600,400));
			FluidWaveform(buf,slicepoints,bounds:Rect(100,100,1400,200),parent:~win);
			~win.front;
		};
	});
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

	s.sync;

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

	~fw = FluidWaveform(~audio,~indices,~pitch_analysis,imageBuffer:~mels,bounds:Rect(100,100,1200,300),imageColorScaling:1,imageColorScheme:1);
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

	~fw = FluidWaveform(~audio,~indices,~pitch_analysis,bounds:Rect(100,100,1200,300),imageColorScaling:1,imageColorScheme:1);
}
)

// adding layers one at a time kinda test
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
// plot some of it, but then...
~fw = FluidWaveform(bounds:Rect(100,450,1200,300));
~fw.addImageLayer(~mels,2,imageColorScaling:1);
~fw.front;
)

// add one feature at a time
~fw.addAudioLayer(~audio,Color(1,1,1,0.5)).refresh;

~fw.addIndicesLayer(~indices,~audio,Color.black).refresh;

~fw.addFeaturesLayer(~pitch_analysis,[Color.cyan,Color.yellow]).refresh;

// check how many layers
~fw.layers

(
// plot differen components with different colors
Routine{
	~audio = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	~resynth = Buffer(s);
	~n_components = 3;
	~resynths = {Buffer(s)} ! ~n_components;
	~mels = {Buffer(s)} ! ~n_components;
	~activations = Buffer(s);
	~harm = {Buffer(s)} ! 2;
	~perc = {Buffer(s)} ! 2;
	~percmels = {Buffer(s)} ! 2;
	~harmmels = {Buffer(s)} ! 2;
	~numBands = 400;
	~windowSize = 4096;
	~hopSize = 256;

	s.sync;

	FluidBufNMF.processBlocking(s,~audio,resynth:~resynth,activations:~activations,components:~n_components,resynthMode: 1);

	2.do{
		arg i;
		FluidBufHPSS.processBlocking(s,~audio,harmonic:~harm[i],percussive:~perc[i],maskingMode:i,harmFilterSize:51,percFilterSize:91);
	};

	~n_components.do{
		arg i;
		FluidBufCompose.processBlocking(s,~resynth,startChan:i,numChans:1,destination:~resynths[i]);
		FluidBufMelBands.processBlocking(s,~resynths[i],features:~mels[i],numBands:~numBands,windowSize:~windowSize,hopSize:~hopSize);
	};

	~harm.do{
		arg buf, i;
		FluidBufMelBands.processBlocking(s,buf,features:~harmmels[i],numBands:~numBands,windowSize:~windowSize,hopSize:~hopSize);
	};

	~perc.do{
		arg buf, i;
		FluidBufMelBands.processBlocking(s,buf,features:~percmels[i],numBands:~numBands,windowSize:~windowSize,hopSize:~hopSize);
	};

	~colors = FluidViewer.createCatColors.collect{
		arg color;
		color;
	};

	s.sync;

	defer{
		~melsfw = FluidWaveform(bounds:Rect(0,0,1600,400));

		~n_components.do{
			arg i;
			~melsfw.addImageLayer(~mels[i],~colors[i],1,1);
		};

		~melsfw.front;

		~binaryfw = FluidWaveform(bounds:Rect(0,400,1600,400));
		~binaryfw.addImageLayer(~percmels[1],~colors[0],1);
		~binaryfw.addImageLayer(~harmmels[1],~colors[1],1);
		~binaryfw.front;

		~softfw = FluidWaveform(bounds:Rect(0,800,1600,400));
		~softfw.addImageLayer(~percmels[0],~colors[0],1);
		~softfw.addImageLayer(~harmmels[0],~colors[1],1);
		~softfw.front;
	};
}.play;
)
