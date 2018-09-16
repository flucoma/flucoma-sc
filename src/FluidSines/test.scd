s.reboot;

b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
b.play

// basic param
{FluidSines.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
{FluidSines.ar(PlayBuf.ar(1,b.bufnum,loop:1),30,MouseX.kr(),5)}.play

// null test (the process add a latency of (( hopSize * minTrackLen) + windowSize) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidSines.ar(sig).sum - DelayN.ar(sig, 1, ((( 512 * 15) + 2048)/ s.sampleRate))]}.play
