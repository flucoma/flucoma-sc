/* ================= FluidSines =================
FluidSines will extract a sound into a sinusoidal and residual component. It does this by trying to recreate the input sound with a sinusoidal model. Anything that it can't confidently form as a sinusoid, is considered "residual".

Useful for separating the stable, pitched components of a sound from the rest.
*/

// sines in L, residual in R
~buf = Buffer.read(s,FluidFilesPath("Tremblay-AaS-SynthTwoVoices-M.wav"));

(
y = {
	var sig = PlayBuf.ar(1,~buf,BufRateScale.ir(~buf),loop:1);
	var sines, residual;
	# sines, residual = FluidSines.ar(sig,detectionThreshold:-40,minTrackLen:2);
	[sines,residual];
}.play;
)

// isolate just sines or residual;
~song = Buffer.readChannel(s,FluidFilesPath("Tremblay-BeatRemember.wav"),channels:[0]);

(
y = {
	arg mix = 0.5;
	var sig = PlayBuf.ar(1,~song,BufRateScale.ir(~song),loop:1);
	var sines, residual;
	# sines, residual = FluidSines.ar(sig);
	sig = SelectX.ar(mix,[sines,residual]);
	sig.dup;
}.play;
)

// just sines
y.set(\mix,0);

// just residual
y.set(\mix,1);

// a stereo example
~song = Buffer.read(s,FluidFilesPath("Tremblay-BeatRemember.wav"));

(
y = {
	arg mix = 0.5;
	var sig = PlayBuf.ar(2,~song,BufRateScale.ir(~buf),loop:1);
	var l, r, sinesL, residualL, sinesR, residualR, sines, residual;
	# l, r = FluidSines.ar(sig);
	# sinesL, residualL = l;
	# sinesR, residualR = r;
	sig = SelectX.ar(mix,[[sinesL,sinesR],[residualL,residualR]]);
	sig;
}.play;
)

// just sines
y.set(\mix,0);

// just residual
y.set(\mix,1);

// send just the 'sines' to a Reverb
(
{
	var sig = PlayBuf.ar(2,~song,BufRateScale.ir(~buf),loop:1).sum * 0.5;
	var sines, residual;
	var latency = ((15 * 512) + 1024 ) / ~song.sampleRate;
	# sines, residual = FluidSines.ar(sig);
	DelayN.ar(sig,latency,latency) + GVerb.ar(sines);
}.play;
)

// send just the 'residual' to a Reverb
(
{
	var sig = PlayBuf.ar(2,~song,BufRateScale.ir(~buf),loop:1).sum * 0.5;
	var sines, residual;
	var latency = ((15 * 512) + 1024 ) / ~song.sampleRate;
	# sines, residual = FluidSines.ar(sig);
	DelayN.ar(sig,latency,latency) + GVerb.ar(residual);
}.play;
)

/* ============== FluidHPSS ===============
FluidHPSS separates a sound into "harmonic" and "percussive" components. This can be useful for material where there is a somewhat realistic basis for these two types to exist, such as in a drum hit. It can also be interesting on material where the two are merged together in more complex ways.
*/

//load a soundfile to play
~buf = Buffer.readChannel(s,FluidFilesPath("Tremblay-BeatRemember.wav"),channels:[0]);

// run with basic parameters (left is harmonic, right is percussive)
{FluidHPSS.ar(PlayBuf.ar(1,~buf,loop:1))}.play

// run in mode 2, listening to:
//the harmonic stream
{FluidHPSS.ar(PlayBuf.ar(1,~buf,loop:1),maskingMode:2)[0].dup}.play
// the percussive stream
{FluidHPSS.ar(PlayBuf.ar(1,~buf,loop:1),maskingMode:2)[1].dup}.play
// the residual stream
{FluidHPSS.ar(PlayBuf.ar(1,~buf,loop:1),maskingMode:2)[2].dup}.play

// do the above again with another sound file
~buf = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));

/* =================== FluidTransients =========================
FluidTransients can separate out transient from residual material. Transient is quite a fuzzy term depending on who you are talking to. Producers might use it to talk about any sound that is bright, loud or percussive while an engineer could be referring to a short, full spectrum change in the signal.

This algorithm is based on a "de-clicking" audio restoration approach.
*/

//load some buffer
~buf = Buffer.read(s,FluidFilesPath("Tremblay-AaS-SynthTwoVoices-M.wav"));

// basic parameters
{FluidTransients.ar(PlayBuf.ar(1, ~buf, loop:1))}.play

// just the transients
{FluidTransients.ar(PlayBuf.ar(1, ~buf, loop:1))[0].dup}.play

// =================== Audio Transport =========================
//load 2 files
(
b = Buffer.read(s,FluidFilesPath("Tremblay-CEL-GlitchyMusicBoxMelo.wav"));
c = Buffer.read(s,FluidFilesPath("Tremblay-CF-ChurchBells.wav"));
)
//listen to them
b.play
c.play
//stereo cross!
{FluidAudioTransport.ar(PlayBuf.ar(2,b,loop: 1),PlayBuf.ar(2,c,loop: 1),MouseX.kr())}.play;
