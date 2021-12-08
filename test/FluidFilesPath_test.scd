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