(
// these will return the same path
(FluidFilesPath()++"Nicol-LoopE-M.wav").postln;
FluidFilesPath("Nicol-LoopE-M.wav").postln;
)

(
// test it one way
s.waitForBoot{
	Routine{
		var path = FluidFilesPath()++"Nicol-LoopE-M.wav";
		var buf = Buffer.read(s,path);

		s.sync;

		buf.play;
	}.play;
}
)

(
// test it another way
s.waitForBoot{
	Routine{
		var path = FluidFilesPath("Nicol-LoopE-M.wav");
		var buf = Buffer.read(s,path);

		s.sync;

		buf.play;
	}.play;
}
)