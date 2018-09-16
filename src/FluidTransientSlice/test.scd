s.reboot;

b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);

// basic param (the process add a latency of (blockSize + padding - order) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidTransientSlice.ar(sig)*0.5, DelayN.ar(sig, 1, ((256 + 128 - 20)/ s.sampleRate))]}.play

// sexier params
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidTransientSlice.ar(sig,order:80,debounce:2205)*0.5, DelayN.ar(sig, 1, ((256 + 128 - 80)/ s.sampleRate))]}.play

// more musical trans-trigged autopan
(
{
	var sig, trig, syncd, pan;
	sig = PlayBuf.ar(1,b.bufnum,loop:1);
	trig = FluidTransientSlice.ar(sig,order:10,debounce:2205);
	syncd = DelayN.ar(sig, 1, ((256 + 128 - 10)/ s.sampleRate));
	pan = TRand.ar(-1,1,trig);
	Pan2.ar(syncd,pan);
}.play
)