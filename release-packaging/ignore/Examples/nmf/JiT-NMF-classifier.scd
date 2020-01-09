s.reboot

//a circular buffer is doing a fake real time and attacks trigger an analysis

(
b = Buffer.alloc(s,s.sampleRate * 2);
g = Bus.audio(s,1);

SynthDef(\JITcircular,{arg bufnum = 0, input = 0, env = 0;
	var head, head2, duration, audioin, halfdur, trig;
	duration = BufFrames.kr(bufnum) / 2;
	halfdur = duration / 2;
	head = Phasor.ar(0,1,0,duration);
	head2 = (head + halfdur) % duration;

	// circular buffer writer
	audioin = In.ar(input,1);
	BufWr.ar(audioin,bufnum,head,0);
	BufWr.ar(audioin,bufnum,head+duration,0);
	trig = FluidAmpSlice.ar(audioin,4410,4410,-60,-70,4410,4410,relRampUp: 10, relRampDown:2205, relThreshOn:12, relThreshOff: 9, highPassFreq: 300);

	// cue the calculations via the language
	SendReply.ar(trig, '/attack',head);

	Out.ar(0,[audioin, trig]);
}).add;

// drum sounds modified from original code by snappizz
// https://sccode.org/1-523

SynthDef(\fluidbd, {
    |out = 0, pan = 0, amp = 0.3|
    var body, bodyFreq, bodyAmp;
    var pop, popFreq, popAmp;
    var click, clickAmp;
    var snd;

    // body starts midrange, quickly drops down to low freqs, and trails off
    bodyFreq = EnvGen.ar(Env([261, 120, 51], [0.035, 0.08], curve: \exp));
    bodyAmp = EnvGen.ar(Env.linen(0.005, 0.1, 0.3), doneAction: 2);
    body = SinOsc.ar(bodyFreq) * bodyAmp;
    // pop sweeps over the midrange
    popFreq = XLine.kr(750, 261, 0.02);
    popAmp = EnvGen.ar(Env.linen(0.001, 0.02, 0.001)) * 0.15;
    pop = SinOsc.ar(popFreq) * popAmp;
    // click is spectrally rich, covering the high-freq range
    // you can use Formant, FM, noise, whatever
    clickAmp = EnvGen.ar(Env.perc(0.001, 0.01)) * 0.15;
    click = LPF.ar(Formant.ar(910, 4760, 2110), 3140) * clickAmp;

    snd = body + pop ; //+ click;
    snd = snd.tanh;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

SynthDef(\fluidsn, {
    |out = 0, pan = 0, amp = 0.3|
    var pop, popAmp, popFreq;
    var noise, noiseAmp;
    var snd;

    // pop makes a click coming from very high frequencies
    // slowing down a little and stopping in mid-to-low
    popFreq = EnvGen.ar(Env([3261, 410, 160], [0.005, 0.01], curve: \exp));
    popAmp = EnvGen.ar(Env.perc(0.001, 0.11)) * 0.7;
    pop = SinOsc.ar(popFreq) * popAmp;
    // bandpass-filtered white noise
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.15), doneAction: 2);
    noise = BPF.ar(WhiteNoise.ar, 810, 1.6) * noiseAmp;

    snd = (pop + noise) * 1.3;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

SynthDef(\fluidhh, {
    |out = 0, pan = 0, amp = 0.3|
    var click, clickAmp;
    var noise, noiseAmp;
    var snd;

    // noise -> resonance -> expodec envelope
    noiseAmp = EnvGen.ar(Env.perc(0.001, 0.3, curve: -8), doneAction: 2);
    noise = Mix(BPF.ar(ClipNoise.ar, [4010, 4151], [0.15, 0.56], [1.0, 0.6])) * 0.7 * noiseAmp;

    snd = noise;

    Out.ar(out, Pan2.ar(snd, pan, amp));
}).add;

)

(
// instantiate the JIT-circular-buffer
x = Synth(\JITcircular,[\bufnum, b.bufnum, \input, g.index]);

// instantiate the listener to cue the processing from the language side

r = OSCFunc({ arg msg;
	msg.postln;
	}, '/attack', s.addr);
)

// stop it all

(
w = Window("Control", Rect(100,100,360,100)).front;

Button(w, Rect(10,10,80, 80)).states_([["bd",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidbd, [\out, g.index], x, \addBefore)});
Button(w, Rect(100,10,80, 80)).states_([["sn",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidsn, [\out, g.index], x, \addBefore)});
Button(w, Rect(190,10,80, 80)).states_([["hh",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidhh, [\out, g.index], x,\addBefore)});
StaticText(w, Rect(280,7,70,25)).string_("Select").align_(\center);
c = PopUpMenu(w, Rect(280,32,70,25)).items_(["train","guess"]);
d = PopUpMenu(w, Rect(280,65,70,25)).items_(["classA","classB","classC"]);
w.onClose_({b.free;g.free;r.clear;x.free; y.free;});
)