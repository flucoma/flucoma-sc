// load two very different files
(
b = Buffer.read(s,File.realpath(FluidBufLoudness.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-SA-UprightPianoPedalWide.wav");
c = Buffer.read(s,File.realpath(FluidBufLoudness.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-AaS-AcousticStrums-M.wav");
)

// composite one on left one on right as test signals
FluidBufCompose.process(s, c, numFrames:b.numFrames, startFrame:555000,destStartChan:1, destination:b)
b.play

// create a buffer as destinations
c = Buffer.new(s);

//run the process on them with EBU standard Instant Loudness of
(
Routine{
    t = Main.elapsedTime;
    FluidBufLoudness.process(s, b, features: c, windowSize: 17640, hopSize:4410);
    (Main.elapsedTime - t).postln;
}.play
)

c = FluidBufThreadDemo.process(s, b, 100000, {|x|x.get(0,{|y|y.postln});});
c.cancel

{FluidLoudness.kr(PinkNoise.ar(0.1),windowSize:17640,hopSize:4410,maxwindowSize:17640)}.scope