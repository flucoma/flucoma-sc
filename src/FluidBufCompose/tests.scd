s.reboot
////////////////////////////
// test for efficiency

(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
c = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-SA-UprightPianoPedalWide.wav".resolveRelative);
d = Buffer.new(s);
)

(
// with basic params (basic summing of each full buffer in all dimensions)
Routine{
	t = Main.elapsedTime;
	FluidBufCompose.process(s, srcBufNumA: b.bufnum, srcBufNumB: c.bufnum, dstBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play;
)

d.query;
d.play;
d.plot

//constructing a mono buffer, with a quiet punch from the synth, with a choked piano resonance from the left channel
(
Routine{
	t = Main.elapsedTime;
	FluidBufCompose.process(s, srcBufNumA: b.bufnum, nFramesA: 9000, srcGainA: 0.5, srcBufNumB: c.bufnum, startAtB:30000, nFramesB:44100, nChansB:1, srcGainB:0.9, dstBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play;
)

//constructing a stereo buffer, with the end of the mono synth in both channels, with a piano resonance in swapped stereo
(
Routine{
	t = Main.elapsedTime;
	FluidBufCompose.process(s, srcBufNumA: b.bufnum, startAtA: 441000, nChansA: 2, srcGainA: 0.6, srcBufNumB: c.bufnum, nFramesB: 80000, startChanB: 1, nChansB: 2, srcGainB: 0.5, dstStartAtB: 22050, dstStartChanB: 0, dstBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play;
)

//constructing a one second buffer: the first second of each buffer, the mono synth on the right, the piano on the left
(
Routine{
	t = Main.elapsedTime;
	FluidBufCompose.process(s, srcBufNumA: b.bufnum, nFramesA: 44100, nChansA: 1, dstStartChanA: 1, srcBufNumB: c.bufnum, nFramesB:44100, nChansB:1, dstBufNum: d.bufnum);
	s.sync;
	(Main.elapsedTime - t).postln;
}.play;
)

// trying to grow a buffer on itself
e = Buffer.alloc(s,1,1);
(
Routine {
	FluidBufCompose.process(s,srcBufNumA: b.bufnum, srcBufNumB: e.bufnum, dstBufNum: e.bufnum);
	s.sync;
	FluidBufCompose.process(s,srcBufNumA: c.bufnum,  nChansA: 1, srcBufNumB: e.bufnum, dstBufNum: e.bufnum);
	s.sync;
}.play;
)

e.plot
e.play