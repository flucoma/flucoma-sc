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

		1.wait;

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

		1.wait;

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

		1.wait;

		defer{buf.plot};
	}.play;
}
)

(
// FluidBufToKr

s.waitForBoot{
	Routine{
		var buf = Buffer.loadCollection(s,Array.fill(1001,{arg i; i}));

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
// This should throw an error because this sound file buffer is longer than 1000 samples
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
// This should throw an error because this sound file buffer is longer than 1000 samples
s.waitForBoot{
	Routine{
		// var buf = Buffer.alloc(s,1000);
		var buf = Buffer.read(s,"/Users/macprocomputer/Desktop/_flucoma/code/flucoma-core-src/AudioFiles/Harker-DS-TenOboeMultiphonics-M.wav");

		s.sync;

		{
			var sig = SinOsc.kr(rrand(1.0.dup(10),4.0));
			FluidKrToBuf.kr(sig,buf);
		}.play;

		3.wait;

		defer{buf.plot};
	}.play;
}
)

// ===================== pass a buffer to a running synth =======================
(
s.waitForBoot{
	Routine{
		~synth = {
			arg buf;
			FluidBufToKr.kr(buf,5).poll;
		}.play;

		2.wait;

		"make buffer".postln;
		~buffer = Buffer.alloc(s,5);
		s.sync;
		~buffer.setn(0,Array.fill(5,{rrand(0,100)}));
		s.sync;
		~synth.set(\buf,~buffer);
	}.play;
};
)

(
// throws error because number of frames not specified
s.waitForBoot{
	Routine{
		~synth = {
			arg buf;
			FluidKrToBuf.kr(SinOsc.kr(Array.fill(5,{rrand(0.0,1.0)})),buf);

			FluidBufToKr.kr(buf).poll; ///////// this will now throw an error asking for a numFrames
		}.play;

		2.wait;

		~buffer = Buffer.alloc(s,5);
		s.sync;
		~synth.set(\buf,~buffer);
	}.play;
};
)

(
// works
s.waitForBoot{
	Routine{
		~synth = {
			arg buf = 999;
			FluidKrToBuf.kr(SinOsc.kr(Array.fill(5,{rrand(0.0,1.0)})),buf);

			FluidBufToKr.kr(buf,5).poll; ////////// this will work becaues it knows how many frames the buffer will be
		}.play;

		2.wait;

		~buffer = Buffer.alloc(s,5);
		s.sync;
		~synth.set(\buf,~buffer);
	}.play;
};
)
