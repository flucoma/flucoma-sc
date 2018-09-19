s.reboot;

b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
b.play

// basic param
{FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// params in mode 1
{FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1),17,31,1,0.05,40,0.1,-40)}.play

// params in mode 2
{FluidHPSS.ar(PlayBuf.ar(1,b.bufnum,loop:1),17,31,2,0.05,40,0.1,-40, 0.1, -10, 0.2, 10)[2].dup}.play



// null test (the process add a latency of ((harmFiltSize + (winSize / hopSize) - 1) *  hopSize) samples
{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,31, winSize:1024,hopSize:512,fftSize:2048).sum - DelayN.ar(sig, 1, ((31 + 1) *  512 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,21, winSize:1024,hopSize:512,fftSize:2048).sum - DelayN.ar(sig, 1, ((21 + 1) *  512 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,29, winSize:1024,hopSize:512,fftSize:2048).sum - DelayN.ar(sig, 1, ((29 + 1) *  512 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,29, winSize:1024,hopSize:256,fftSize:2048).sum - DelayN.ar(sig, 1, ((29 + 3) *  256 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,11,29, winSize:2048,hopSize:512,fftSize:4096).sum - DelayN.ar(sig, 1, ((29 + 3) *  512 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,21, winSize:2048,hopSize:256,fftSize:4096).sum - DelayN.ar(sig, 1, ((21 + 7) *  256 / s.sampleRate))]}.play


{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,21,1,1,winSize:1024,hopSize:512,fftSize:2048).sum - DelayN.ar(sig, 1, ((21 + 1) *  512 / s.sampleRate))]}.play

{var sig = PlayBuf.ar(1,b.bufnum,loop:1); [FluidHPSS.ar(sig,17,21,1,2,winSize:1024,hopSize:512,fftSize:2048).sum - DelayN.ar(sig, 1, ((21 + 1) *  512 / s.sampleRate))]}.play
