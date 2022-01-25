(
// og test
s.waitForBoot{

	Task{
		var buf = Buffer.read(s,"/Users/macprocomputer/Desktop/_flucoma/code/flucoma-core-src/AudioFiles/Nicol-LoopE-M.wav");
		var slicepoints = Buffer(s);

		FluidBufAmpSlice.process(s,buf,indices:slicepoints,fastRampUp:10,fastRampDown:2205,slowRampUp:4410,slowRampDown:4410,onThreshold:10,offThreshold:5,floor:-40,minSliceLength:4410,highPassFreq:20,action:{
			FluidWaveform(buf,slicepoints,bounds:Rect(0,0,1600,400));
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

	// s.sync;

	~fw.addImageLayer(~mels,1,imageColorScaling:1);

	// s.sync;

	~fw.addAudioLayer(~audio);

	// s.sync;

	~fw.addIndicesLayer(~indices,~audio,Color.yellow);

	~fw.addFeaturesLayer(~pitch_analysis);
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

	s.sync;
	~fw.addImageLayer(~mels,1,1);
	s.sync;

	~fw.addAudioLayer(~audio,Color.red);

	s.sync;

	~fw.addIndicesLayer(~indices,~audio,Color.yellow);

	~fw.addFeaturesLayer(~pitch_analysis,Color.cyan,true);
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
// error because there are all sorts of race conditions happening
~fw = FluidWaveform(bounds:Rect(100,100,1200,300));
~fw.addImageLayer(~mels,1,imageColorScaling:1);
~fw.addAudioLayer(~audio);
~fw.addIndicesLayer(~indices,~audio,Color.yellow);
~fw.addFeaturesLayer(~pitch_analysis);
~fw.front;
)