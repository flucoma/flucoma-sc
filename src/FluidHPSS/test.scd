s.reboot;

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");
b.play

// basic param
{FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
{FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1),17,31,0,0,1024,512,2048)}.play





// null test (the process add a latency of ((harmonicFilterSize +1) *  windowSize / 2) samples NOT WORKING ARGGGG
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,21,0,0,1024,512,2048).sum , DelayN.ar(sig, 1, ((31 + 1) *  1024 / 2 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,31,0,0,1024,512,2048).sum - DelayN.ar(sig, 1, ((31 + 5) *  512 / 2 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,29,0,0,1024,512,2048).sum - DelayN.ar(sig, 1, ((29 + 7) *  512 / 2 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,29,0,0,1024,256,2048).sum - DelayN.ar(sig, 1, (40 *  256 / 2 / s.sampleRate))]}.play



//((harmonicFilterSize + (overlap - 1)) *  hopsize)
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,31,0,0,1024,512,2048).sum - DelayN.ar(sig, 1, (((31 + (4 - 1)) *  512) / s.sampleRate))]}.play



// half harmoSize+1 * hop + window
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,31,0,0,1024,512,2048).sum - DelayN.ar(sig, 1, ((((31+1) / 2 * 512) + 1024) / s.sampleRate))]}.play
