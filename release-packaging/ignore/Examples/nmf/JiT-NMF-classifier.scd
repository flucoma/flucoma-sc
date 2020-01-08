s.reboot

//a circular buffer is doing a fake real time and attacks trigger an analysis

(
b = Buffer.alloc(s,s.sampleRate * 2);
g = Bus.audio(s,1);

SynthDef(\JITcircular,{arg bufnum = 0, input = 0, env = 0;
	var head, head2, duration, audioin, halfdur;
	duration = BufFrames.kr(bufnum) / 2;
	halfdur = duration / 2;
	head = Phasor.ar(0,1,0,duration);
	head2 = (head + halfdur) % duration;

	// circular buffer writer
	audioin = In.ar(input,1);
	BufWr.ar(audioin,bufnum,head,0);
	BufWr.ar(audioin,bufnum,head+duration,0);

	// cue the calculations via the language
	SendReply.ar(FluidOnsetSlice.ar(audioin,9,0.1,5), '/attack',head);

	Out.ar(0,[audioin, FluidOnsetSlice.ar(audioin,9,0.1,5)]);
}).add;
)

x = {Impulse.ar(0.3)}.play(s,g.index)

// instantiate the JIT-circular-buffer
y = Synth(\JITcircular,[\bufnum, b.bufnum, \input, g.index], x, 'addAfter');

// instantiate the listener to cue the processing from the language side

(
w = OSCFunc({ arg msg;
	msg.postln;
	}, '/attack', s.addr);
)

// stop it all
b.free;g.free;w.clear;x.free; y.free;
