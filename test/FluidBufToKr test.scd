(
// FluidKrToBuf test
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
// FluidBufToKr
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
// test both
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

(
// FluidKrToBuf --- kr is longer than buf...
// it just doesnt write all the values of kr into the buffer
s.waitForBoot{
	Routine{
		var buf = Buffer.alloc(s,5);

		s.sync;

		{
			var sig = SinOsc.kr(rrand(1.0.dup(6),4.0));
			FluidKrToBuf.kr(sig,buf);
		}.play;

		3.wait;

		defer{buf.plot};
	}.play;
}
)

(
// FluidKrToBuf --- kr is shorter than buf...
// the last index of the buffer is just not being written into, it's still zero
s.waitForBoot{
	Routine{
		var buf = Buffer.alloc(s,5);

		s.sync;

		{
			var sig = SinOsc.kr([1,2,3,4]);
			FluidKrToBuf.kr(sig,buf);
		}.play;

		3.wait;

		defer{buf.plot};
	}.play;
}
)

(
// FluidBufToKr
// 100 is fine
/// 1000 is fine
s.waitForBoot{
	Routine{
		var buf = Buffer.loadCollection(s,Array.fill(1000,{arg i; i}));

		s.sync;

		{
			var sig = FluidBufToKr.kr(buf);
			sig.poll;
		}.play;
	}.play;
}
)

(
// FluidBufToKr
// 100 is fine
/// 1000 is fine
s.waitForBoot{
	Routine{
		var buf = Buffer.read(s,"/Users/macprocomputer/Desktop/_flucoma/code/flucoma-core-src/AudioFiles/Harker-DS-TenOboeMultiphonics-M.wav");

		s.sync;

		{
			var sig = FluidBufToKr.kr(buf);
			sig.poll;
		}.play;
	}.play;
}
)

(
// FluidKrToBuf test with super long buffer
// 100 is fine
// 1000 is fine
s.waitForBoot{
	Routine{
		// var buf = Buffer.alloc(s,1000);
		var buf = Buffer.read(s,"/Users/macprocomputer/Desktop/_flucoma/code/flucoma-core-src/AudioFiles/Harker-DS-TenOboeMultiphonics-M.wav");

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

