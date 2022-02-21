//load a part of a sound that has 3 clear components: a clear pitch component to start, a noisy pitchless ending and DC offset silence on both ends
(
b = Buffer.read(s,FluidFilesPath("Tremblay-ASWINE-ScratchySynth-M.wav"),42250,44100);
~pitches = Buffer(s);
~stats = Buffer(s);
~loud = Buffer(s);
~scaled = Buffer(s);
~brutePitchStats = Array;
~loudnessWeighedPitchStats = Array;
~confidenceWeighedPitchStats = Array;
~condidenceWeightedPitchIQRStats = Array;
~pitchIQRStats = Array;
)

b.play
b.plot

FluidBufPitch.process(s,b,features: ~pitches)
FluidBufStats.process(s,~pitches,stats:~stats)
~stats.getn(0,14,{|x|~brutePitchStats = x; x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})

//observe the data - there are something clearly "wrong" in there - mostly, should we consider the stats on pitch when the confidence is low?
~pitches.plot(separately: true)

//let's check the loudness
FluidBufLoudness.process(s,b,features: ~loud)
FluidBufStats.process(s,~loud,stats:~stats)
~stats.getn(0,14,{|x|x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})

~loud.plot(separately: true)

//it seems the loudness of the noisy section will not help us reject the silence. But let's try
FluidBufScale.process(s,~loud,numChans: 1,destination: ~scaled,inputLow: -60,inputHigh: -20)
FluidBufStats.process(s,~pitches, stats:~stats,weights: ~scaled)
~stats.getn(0,14,{|x|~loudnessWeighedPitchStats = x; x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})
//not much difference but let's listen

//average pitch
c = {SinOsc.ar(~brutePitchStats[0],mul: 0.05)}.play
//compare with the source
b.play
c.free
//loudness-weighted average
c = {SinOsc.ar(~loudnessWeighedPitchStats[0],mul: 0.05)}.play
//compare with the source
b.play
c.free
//hmmm, worse! That is because we did remove the low amplitude skewing to wards the default value (high) which was balancing our noisy peak with low pitch and low pitch confidence...

//let's instead weight against the pitch confidence, first applying a threshold to so we pull down any middle value we want to ignore
FluidBufThresh.process(s, ~pitches, startChan: 1, numChans: 1, destination: ~scaled, threshold: 0.8)
FluidBufStats.process(s,~pitches, stats:~stats,weights: ~scaled)
~stats.getn(0,14,{|x|~confidenceWeighedPitchStats = x;x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})

//let's listen
c = {SinOsc.ar(~confidenceWeighedPitchStats[0],mul: 0.05)}.play
//compare with the source
b.play
c.free
// much better! it is even better when we move the threshold above but 0.8 confidence is quite high...  If we look at our stats we see that there are still minima in the low hundreds, and maxima in the very top...These must be statistically far enough and few enough just to mess a bit our stats, so let's use the inter-quantile range to first remove them then compute the stats.
FluidBufStats.process(s,~pitches, stats:~stats,weights: ~scaled,outliersCutoff: 1.5)
~stats.getn(0,14,{|x|~confidenceWeightedPitchIQRStats = x;x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})
//now that is impressive!
c = {SinOsc.ar(~confidenceWeightedPitchIQRStats[0],mul: 0.05)}.play
b.play
c.free

//for completion, here is just with rejection of outliers - not as good, but a decent second best!
FluidBufStats.process(s,~pitches, stats:~stats,outliersCutoff: 1.5)
~stats.getn(0,14,{|x|~pitchIQRStats = x;x.reshape(7,2).do{|y| "%\t\t\t%\n".postf(y[0].round(0.1),y[1].round(0.01))}})
c = {SinOsc.ar(~pitchIQRStats[0],mul: 0.05)}.play
b.play
c.free
