// using nmf in 'real-time' as a classifier
// how it works: a circular buffer is recording and attacks trigger the process
// if in learning mode, it does a one component nmf which makes an approximation of the base. 3 of those will be copied in 3 different positions of our final 3-component base
// in in guessing mode, it does a thres component nmf from the trained bases and yields the 3 activation peaks, on which it thresholds resynth

//how to use:
// 1. start the server
// 2. select between parenthesis below and execute. You should get a window with 3 pads (bd sn hh) and various menus
// 3. train the 3 classes:
//    3.1 select the learn option
//    3.2 select which class you want to train
//    3.3 play the sound you want to associate with that class a few times (the left audio channel is the source)
//    3.4 click the transfer button
//    3.5 repeat (3.2-3.4) for the other 2 classes.
//    3.x you can observe the 3 bases here:
~classify_bases.plot(numChannels:3)

// 4. classify
//    4.1 select the classify option
//    4.2 press a pad and look at the activation
//    4.3 tweak the thresholds and enjoy the resynthesis. (the right audio channel is the detected class where classA is a bd sound)
//    4.x you can observe the 3 activations here:
~activations.plot(numChannels:3)

/// code to execute first
(
var circle_buf = Buffer.alloc(s,s.sampleRate * 2); // b
var input_bus = Bus.audio(s,1); // g
var classifying = 0; // c
var cur_training_class = 0; // d
var train_base = Buffer.alloc(s, 65); // e
var activation_vals = [0.0,0.0,0.0]; // j
var thresholds = [0.5,0.5,0.5]; // k
var activations_disps;
var analysis_synth;
var osc_func;
var update_rout;

~classify_bases = Buffer.alloc(s, 65, 3); // f
~activations = Buffer.new(s);

// the circular buffer with triggered actions sending the location of the head at the attack
Routine {
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
		trig = FluidAmpSlice.ar(audioin, 10, 1666, 2205, 2205, 12, 9, -47,4410, 85);

		// cue the calculations via the language
		SendReply.ar(trig, '/attack',head);

		Out.ar(0,audioin);
	}).add;

	// drum sounds taken from original code by snappizz
	// https://sccode.org/1-523
	// produced further and humanised by PA
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

	// makes sure all the synthdefs are on the server
	s.sync;

	// instantiate the JIT-circular-buffer
	analysis_synth = Synth(\JITcircular,[\bufnum, circle_buf, \input, input_bus]);
	train_base.fill(0,65,0.1);

	// instantiate the listener to cue the processing from the language side
	osc_func = OSCFunc({ arg msg;
		var head_pos = msg[3];
		// when an attack happens
		if (classifying == 0, {
			// if in training mode, makes a single component nmf
			FluidBufNMF.process(s, circle_buf, head_pos, 128, bases:train_base, basesMode: 1, windowSize: 128);
		}, {
			// if in classifying mode, makes a 3 component nmf from the pretrained bases and compares the activations with the set thresholds
			FluidBufNMF.process(s, circle_buf, head_pos, 128, components:3, bases:~classify_bases, basesMode: 2, activations:~activations, windowSize: 128, action:{
				// we are retrieving and comparing against the 2nd activation, because FFT processes are zero-padded on each sides, therefore the complete 128 samples are in the middle of the analysis.
				~activations.getn(3,3,{|x|
					activation_vals = x;
					if (activation_vals[0] >= thresholds[0], {Synth(\fluidbd,[\out,1])});
					if (activation_vals[1] >= thresholds[1], {Synth(\fluidsn,[\out,1])});
					if (activation_vals[2] >= thresholds[2], {Synth(\fluidhh,[\out,1])});
					defer{
						activations_disps[0].string_("A:" ++ activation_vals[0].round(0.01));
						activations_disps[1].string_("B:" ++ activation_vals[1].round(0.01));
						activations_disps[2].string_("C:" ++ activation_vals[2].round(0.01));
					};
				});
			};
			);
		});
	}, '/attack', s.addr);

	// make sure all the synths are instantiated
	s.sync;

	// GUI for control
	{
		var win = Window("Control", Rect(100,100,610,100)).front;

		Button(win, Rect(10,10,80, 80)).states_([["bd",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidbd, [\out, input_bus], analysis_synth, \addBefore)});
		Button(win, Rect(100,10,80, 80)).states_([["sn",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidsn, [\out, input_bus], analysis_synth, \addBefore)});
		Button(win, Rect(190,10,80, 80)).states_([["hh",Color.black,Color.white]]).mouseDownAction_({Synth(\fluidhh, [\out, input_bus], analysis_synth,\addBefore)});
		StaticText(win, Rect(280,7,85,25)).string_("Select").align_(\center);
		PopUpMenu(win, Rect(280,32,85,25)).items_(["learn","classify"]).action_({|value|
			classifying = value.value;
			if(classifying == 0, {
				train_base.fill(0,65,0.1)
			});
		});
		PopUpMenu(win, Rect(280,65,85,25)).items_(["classA","classB","classC"]).action_({|value|
			cur_training_class = value.value;
			train_base.fill(0,65,0.1);
		});
		Button(win, Rect(375,65,85,25)).states_([["transfer",Color.black,Color.white]]).mouseDownAction_({
			if(classifying == 0, {
				// if training
				FluidBufCompose.process(s, train_base, numChans:1, destination:~classify_bases, destStartChan:cur_training_class);
			});
		});
		StaticText(win, Rect(470,7,75,25)).string_("Acts");
		activations_disps = Array.fill(3, {arg i;
			StaticText(win, Rect(470,((i+1) * 20 )+ 7,80,25));
		});
		StaticText(win, Rect(540,7,55,25)).string_("Thresh").align_(\center);
		3.do {arg i;
			TextField(win, Rect(540,((i+1) * 20 )+ 7,55,25)).string_("0.5").action_({|x| thresholds[i] = x.value.asFloat;});
		};

		win.onClose_({circle_buf.free;input_bus.free;osc_func.clear;analysis_synth.free;});
	}.defer;
}.play;
)

// thanks to Ted Moore for the SC code cleaning and improvements!
