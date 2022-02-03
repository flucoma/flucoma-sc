// A  complex example of using composition as an Mid-Side FIR filtering process

// load a stereo buffer and initialise the many destinations
(
b = Buffer.read(s,FluidFilesPath("Tremblay-SA-UprightPianoPedalWide.wav"));
c = Buffer.new(s);
d = Buffer.new(s);
e = Buffer.new(s);
f = Buffer.new(s);
)

// encode the mid (in c) and the side (in d)
(
FluidBufCompose.process(s,b, numChans: 1, gain: -3.0.dbamp, destination: c);
FluidBufCompose.process(s,b, numChans: 1, gain: -3.0.dbamp, destination: d);
FluidBufCompose.process(s,b, numChans: 1, gain: -3.0.dbamp, startChan: 1, destination: c,  destGain: 1.0);
FluidBufCompose.process(s,b, numChans: 1, gain: -3.0.dbamp * -1.0, startChan: 1, destination: d,  destGain: 1.0);
)

// (optional) compare auraly the stereo with the MS
b.play;
{PlayBuf.ar(1, [c,d])}.play;

//check nullsum with real-time versions
{PlayBuf.ar(1, c) - PlayBuf.ar(2, b).madd(-3.0.dbamp).sum}.play;
{PlayBuf.ar(1, d) - PlayBuf.ar(2, b).madd(-3.0.dbamp * [1,-1]).sum}.play;

// The geeky bit: copy the side (buffer d) on itself with specific amplitudes and delays, in effect applying a FIR filter through expensive convolution

// Important: do either of the 3 options below

// option 1: apply a high pass on the side, with a cutoff of nyquist / 4
e.free; e = Buffer.new(s);
(
[1.0, -1.0].do({ arg x,y;
	FluidBufCompose.process(s, d, gain: x, destStartFrame: y, destination: e, destGain: 1.0);
});
)

// option 2: apply a high pass on the side, with a cutoff of nyquist / 10
e.free; e = Buffer.new(s);
(
[0.8, -0.32, -0.24, -0.16, -0.08].do({ arg x,y;
	FluidBufCompose.process(s, d, gain: x, destStartFrame: y, destination: e, destGain: 1.0);
});
)

// option 3: apply a high pass on the side, with a cutoff of nyquist / 100
e.free; e = Buffer.new(s);
(
[0.982494, -0.066859, -0.064358, -0.061897, -0.059477, -0.057098, -0.054761, -0.052466, -0.050215, -0.048007, -0.045843, -0.043724, -0.041649, -0.03962, -0.037636, -0.035697, -0.033805, -0.031959, -0.030159, -0.028406, -0.026699, -0.025038, -0.023425, -0.021857, -0.020337].do({ arg x,y;
	FluidBufCompose.process(s, d, gain: x, destStartFrame: y, destination: e, destGain: 1.0);
});
)

// play the high-passed side buffer
e.play;
// if you want to try the other filters, do not forget to clear the destination buffer since it will add programmatically onto itself and would not create the expected frequency response

// decode the MS back to stereo
(
FluidBufCompose.process(s,c, numChans: 2, gain: -3.0.dbamp, destination: f);
FluidBufCompose.process(s,e, gain: -3.0.dbamp, destination: f,  destGain: 1.0);
FluidBufCompose.process(s,e, gain: -3.0.dbamp * -1.0, destination: f, destStartChan: 1, destGain: 1.0);
)

// play the MS processed version
f.play;

// compare with the original
b.play;
