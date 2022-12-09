/* ======= 1. Hear the Sound ============

load a part of a sound that has 3 clear components:
- a clear pitch component to start
- a noisy pitchless ending
- DC offset silence on both ends

*/

(
~src = Buffer.read(s,FluidFilesPath("Tremblay-ASWINE-ScratchySynth-M.wav"));//,42250,44100);
)

// listen
~src.play;

// ======= Let's try to extract that frequency from the audio file. ===========

// analyze
~pitches = Buffer(s);
~stats = Buffer(s);

FluidBufPitch.process(s,~src,features: ~pitches);
FluidBufStats.process(s,~pitches,stats:~stats);

(
// get the average freq;
~stats.get(0,{
	arg f;
	~avgfreq = f;
	~avgfreq.postln;
});
)

(
// play a sine tone at the avg freq alongside the soundfile
//average freq
~avgfreq_synth = {SinOsc.ar(~avgfreq,mul: 0.05)}.play;
//compare with the source
~src.play;
)

// hmm... that seems wrong...

/*

what if we weight the average frequency by the loudness
of the analysis frame so that the silences are not considered
as strongly.

*/

// do a loudness analysis
~loud = Buffer(s);
FluidBufLoudness.process(s,~src,features:~loud);
FluidBufStats.process(s,~loud,stats:~stats);

(
// get min and max
~stats.loadToFloatArray(action:{
	arg stats;
	~min_loudness = stats.clump(2).flop[0][4];
	~max_loudness = stats.clump(2).flop[0][6];
	~min_loudness.postln;
	~max_loudness.postln;
});
)

// scale the loudness analysis from 0 to 1, using the min and max gotten above
~scaled = Buffer(s);
FluidBufScale.process(s,~loud,numChans: 1,destination: ~scaled,inputLow: ~min_loudness,inputHigh: ~max_loudness);

// then use this scaled analysis to weight the statistical analysis
FluidBufStats.process(s,~pitches,numChans:1,stats:~stats,weights:~scaled);

(
// get the average freq (now with the weighted average)
~stats.get(0,{
	arg f;
	~avgfreq = f;
	~avgfreq.postln;
});
)

(
// play a sine tone at the avg freq alongside the soundfile
//average freq
~avgfreq_synth = {SinOsc.ar(~avgfreq,mul: 0.05)}.play;
//compare with the source
~src.play;
)

// hmm... still wrong. too low now.

/*
ok, how about if we weight not by loudness, but by the pitch confidence of the pitch analysis
*/

FluidBufPitch.process(s,~src,features: ~pitches);
~thresh_buf = Buffer(s);
FluidBufThresh.process(s, ~pitches, startChan: 1, numChans: 1, destination: ~thresh_buf, threshold: 0.8)
FluidBufStats.process(s,~pitches,numChans:1,stats:~stats,weights:~thresh_buf);

(
// get the average freq
~stats.get(0,{
	arg f;
	~avgfreq = f;
	~avgfreq.postln;
});
)

(
// play a sine tone at the avg freq alongside the soundfile
//average freq
~avgfreq_synth = {SinOsc.ar(~avgfreq,mul: 0.05)}.play;
//compare with the source
~src.play;
)

// closer!

FluidBufPitch.process(s,~src,features: ~pitches);

(
~pitches.loadToFloatArray(action:{
	arg pitches;
	defer{pitches.histo(50,1000,20000).plot(discrete:true)};
});
)
// raise the threshold and toss out some outliers
FluidBufPitch.process(s,~src,features: ~pitches);
~pitches.plot(separately:true);
~thresh_buf = Buffer(s);
FluidBufThresh.process(s, ~pitches, startChan: 1, numChans: 1, destination: ~thresh_buf, threshold: 0.9)
FluidBufStats.process(s,~pitches,numChans:1,stats:~stats,weights:~thresh_buf,outliersCutoff:1.5);

(
// get the average freq
~stats.get(0,{
	arg f;
	~avgfreq = f;
	~avgfreq.postln;
});
)

(
// play a sine tone at the avg freq alongside the soundfile
//average freq
~avgfreq_synth = {SinOsc.ar(~avgfreq,mul: 0.05)}.play;
//compare with the source
~src.play;
)

// further investigations are also in the Examples/dataset/1-learning examples/10b-weighted-pitch-comparison.scd