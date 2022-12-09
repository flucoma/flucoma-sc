s.reboot

//this patch does just-in-time nmf processes on buffer, faking a slightly delayed real-time version of it
//what is happening:
//a circular buffer is doing a fake real time - every half second, it sends a frame to be proceesed by NMF~, requesting 3 ranks. Because this latter process is randomly seeded and not sorted, the 3 ranks are not getting similar results each time, hence the random pan

(
b = Buffer.alloc(s,s.sampleRate * 2);
c = Buffer.new(s,0,3);
d = Buffer.new(s,0,3);
e = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
g = Bus.audio(s,1);
h = Buffer.loadCollection(s, Signal.rectWindow(22491).fade(0,440).fade(22049,22489,1,0).put(22490,0));

SynthDef(\becauseIcan,{arg bufnum = 0, nmfa = 0, nmfb = 0, input = 0, env = 0;
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
	SendReply.ar(head > 500, '/processplease',2);
	SendReply.ar(head > (halfdur + 500), '/processplease',1);

	// read the 2 buffers with an envelop
	Out.ar(0, Splay.ar(BufRd.ar(3,nmfa,head,0,1) * BufRd.ar(1,env,head,0,1)) + Splay.ar(BufRd.ar(3,nmfb,head2,0,1) * BufRd.ar(1,env,head2,0,1)));
}).add;

SynthDef(\playa, { arg output = 0, bufnum = 0;
	Out.ar(output,PlayBuf.ar(1,bufnum,loop:1));
}).add;
)

// instantiate the player
x = Synth(\playa,[\output, g.index, \bufnum, e.bufnum]);

// instantiate the processor, please ignore the Buffer UGen warnings
y = Synth(\becauseIcan,[\bufnum, b.bufnum, \nmfa, c.bufnum, \nmfb, d.bufnum, \input, g.index, \env, h.bufnum], x, 'addAfter');

// instantiate the listener to cue the processing from the language side
(
w = OSCFunc({ arg msg;
			if(msg[3]== 1, {
			FluidBufNMF.process(s, b, numFrames: 22500, resynth: c.bufnum, resynthMode: 1, components: 3, fftSize: 1024, windowSize: 512, hopSize: 256);
	}, {
			FluidBufNMF.process(s, b, 22050, 22500, resynth: d.bufnum, resynthMode: 1, components: 3, fftSize: 1024, windowSize: 512, hopSize: 256);
	});}, '/processplease', s.addr);
)

// stop it all
b.free;c.free;d.free;e.free;f.free;g.free;w.clear;x.free; y.free;
