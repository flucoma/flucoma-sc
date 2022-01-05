(
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