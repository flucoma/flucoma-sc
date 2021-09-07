(
s.waitForBoot{
	Routine{
		var buf = Buffer.alloc(s,5);

		s.sync;

		{
			var sig = SinOsc.kr(rrand(1.0.dup(buf.numFrames),4.0));
			FluidKrToBuf.kr(sig,buf);
		}.play;

		3.wait;

		defer{buf.plot};
	}.play;
}
)

(
s.waitForBoot{
	Routine{
		var buf = Buffer.loadCollection(s,[0,1,2,3,4,7]);

		s.sync;

		{
			var sig = FluidBufToKr.kr(buf);
			sig.poll;
		}.play;
}.play;
}
)

(
s.waitForBoot{
	Routine{
		{
			var buf = LocalBuf(5);
			var insig = SinOsc.kr(rrand(1.0.dup(buf.numFrames),4.0));
			var outsig;
			FluidKrToBuf.kr(insig,buf);
			outsig = FluidBufToKr.kr(buf);
			outsig.poll;
		}.play;
	}.play;
}
)
