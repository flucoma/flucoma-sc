s.reboot;

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");
b.play

// basic param
{FluidSines.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
{FluidSines.ar(PlayBuf.ar(1,b.bufnum,loop:1),windowSize:1024)}.play

// null test (the process add a latency of (( hopSize * minTrackLen) + windowSize) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidSines.ar(sig).sum - DelayN.ar(sig, 1, ((( 512 * 15) + 2048)/ s.sampleRate))]}.play