(
// should all return the same path:
(FluidFilesPath()++"Nicol-LoopE-M.wav").postln;
(FluidFilesPath()+/+"Nicol-LoopE-M.wav").postln;
FluidFilesPath("Nicol-LoopE-M.wav").postln;
FluidFilesPath("/Nicol-LoopE-M.wav").postln;
)

(
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
s.waitForBoot{
	Routine{
		var path = FluidFilesPath("Nicol-LoopE-M.wav");
		var buf = Buffer.read(s,path);

		s.sync;

		buf.play;
	}.play;
}
)