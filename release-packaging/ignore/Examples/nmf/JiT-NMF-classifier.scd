s.reboot

//a circular buffer is doing a fake real time and attacks trigger an analysis

(
b = Buffer.alloc(s,s.sampleRate * 2);
g = Bus.audio(s,1);
c = 0;
d = 0;

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

// drum sounds taken from original code by snappizz
// https://sccode.org/1-523
// produced further and randomised by PA

SynthDef(\fluidbd, {
	|out = 0|
	var body, bodyFreq, bodyAmp;
	var pop, popFreq, popAmp;
	var click, clickAmp;
	var snd;

	// body starts midrange, quickly drops down to low freqs, and trails off
	bodyFreq = EnvGen.ar(Env([Rand(200,300), 120, Rand(45,49)], [0.035, Rand(0.07,0.1)], curve: \exp));
	bodyAmp = EnvGen.ar(Env([0,Rand(0.8,1.3),1,0],[0.005,Rand(0.08,0.085),Rand(0.25,0.35)]), doneAction: 2);
	body = SinOsc.ar(bodyFreq) * bodyAmp;
	// pop sweeps over the midrange
	popFreq = XLine.kr(Rand(700,800), Rand(250,270), Rand(0.018,0.02));
	popAmp = EnvGen.ar(Env([0,Rand(0.8,1.3),1,0],[0.001,Rand(0.018,0.02),Rand(0.0008,0.0013)]));
	pop = SinOsc.ar(popFreq) * popAmp;
	// click is spectrally rich, covering the high-freq range
	// you can use Formant, FM, noise, whatever
	clickAmp = EnvGen.ar(Env.perc(0.001,Rand(0.008,0.012),Rand(0.07,0.12),-5));
	click = RLPF.ar(VarSaw.ar(Rand(900,920),0,0.1), 4760, 0.50150150150) * clickAmp;

	snd = body + pop + click;
	snd = snd.tanh;

	Out.ar(out, snd);
}).add;

SynthDef(\fluidsn, {
	|out = 0|
	var pop, popAmp, popFreq;
	var noise, noiseAmp;
	var click;
	var snd;

	// pop makes a click coming from very high frequencies
	// slowing down a little and stopping in mid-to-low
	popFreq = EnvGen.ar(Env([Rand(3210,3310), 410, Rand(150,170)], [0.005, Rand(0.008,0.012)], curve: \exp));
	popAmp = EnvGen.ar(Env.perc(0.001, Rand(0.1,0.12), Rand(0.7,0.9),-5));
	pop = SinOsc.ar(popFreq) * popAmp;
	// bandpass-filtered white noise
	noiseAmp = EnvGen.ar(Env.perc(0.001, Rand(0.13,0.15), Rand(1.2,1.5),-5), doneAction: 2);
	noise = BPF.ar(WhiteNoise.ar, 810, 1.6) * noiseAmp;

	click = Impulse.ar(0);
	snd = (pop  + click + noise) * 1.4;

	Out.ar(out, snd);
}).add;

SynthDef(\fluidhh, {
	|out = 0|
	var click, clickAmp;
	var noise, noiseAmp, noiseFreq;

	// noise -> resonance -> expodec envelope
	noiseAmp = EnvGen.ar(Env.perc(0.001, Rand(0.28,0.3), Rand(0.4,0.6), [-20,-15]), doneAction: 2);
	noiseFreq = Rand(3900,4100);
	noise = Mix(BPF.ar(ClipNoise.ar, [noiseFreq, noiseFreq+141], [0.12, 0.31], [2.0, 1.2])) * noiseAmp;

	Out.ar(out, noise);
}).add;
)

(
// instantiate the JIT-circular-buffer
x = Synth(\JITcircular,[\bufnum, b.bufnum, \input, g.index]);

// instantiate the listener to cue the processing from the language side

r = OSCFunc({ arg msg;
	if (c == 0, {
		msg[0].postln;
	}, {
		msg[3].postln;
	});
}, '/attack', s.addr);
)

// stop it all

(
w = Window("Control", Rect(100,100,360,100)).front;

Button(w, Rect(10,10,80, 80)).states_([["bd",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidbd, [\out, g.index], x, \addBefore)});
Button(w, Rect(100,10,80, 80)).states_([["sn",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidsn, [\out, g.index], x, \addBefore)});
Button(w, Rect(190,10,80, 80)).states_([["hh",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidhh, [\out, g.index], x,\addBefore)});
StaticText(w, Rect(280,7,70,25)).string_("Select").align_(\center);
PopUpMenu(w, Rect(280,32,70,25)).items_(["train","guess"]).action_({|value| c = value.value;});
e = PopUpMenu(w, Rect(280,65,70,25)).items_(["classA","classB","classC"]).action_({|value| d = value.value;});
w.onClose_({b.free;g.free;r.clear;x.free; y.free;});
)

