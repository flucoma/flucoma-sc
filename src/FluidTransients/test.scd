s.reboot;

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");

// basic param
{FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
{FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1),80,threshFwd:MouseX.kr(0,5),threshBack:MouseY.kr(0,2))}.play

// null test (the process add a latency of (blockSize + padding - order) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidTransients.ar(sig).sum - DelayN.ar(sig, 1, ((256 + 128 - 20)/ s.sampleRate))]}.play