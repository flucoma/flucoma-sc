s.reboot;

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");

// basic param
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play //change to something sensible

// null test (the process add a latency of (blockSize + padding - order) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidTransients.ar(sig).sum - DelayN.ar(sig, 1, ((256 + 128 - 20)/ s.sampleRate))]}.play