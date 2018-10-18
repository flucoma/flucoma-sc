s.reboot;


(
b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-AcousticStrums-M.wav".resolveRelative);
c = Buffer.new(s);
x = Buffer.new(s);
)


(
Routine {
	FluidBufNMF.process(s,b.bufnum,0,88200,0,1, c.bufnum, x.bufnum, rank:10);
	s.sync;
	c.query;
}.play;
)


{FluidNMFMatch.kr(PlayBuf.ar(1,b.bufnum),x.bufnum,10)}.play

