(
// decompose!
s.waitForBoot{
	Routine{
		var drums = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
		var resynth = Buffer(s);
		var n_components = 2;
		FluidBufNMF.process(s,drums,resynth:resynth,components:n_components,resynthMode: 1).wait;

		"original sound".postln;
		{
			PlayBuf.ar(1,drums,BufRateScale.ir(drums),doneAction:2).dup;
		}.play;

		(drums.duration + 1).wait;

		n_components.do{
			arg i;

			"decomposed part #%".format(i+1).postln;
			{
				PlayBuf.ar(n_components,resynth,BufRateScale.ir(resynth),doneAction:2)[i].dup;
			}.play;

			(drums.duration + 1).wait;
		};

		"all decomposed parts spread across the stereo field".postln;

		{
			Splay.ar(PlayBuf.ar(n_components,resynth,BufRateScale.ir(resynth),doneAction:2));
		}.play;

	}.play;
}
)

// ok so what is it doing?
(
Routine{
	var n_components = 2;
	var drums = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	~bases = Buffer(s);
	~activations = Buffer(s);
	~resynth = Buffer(s);
	FluidBufNMF.process(s,drums,bases:~bases,activations:~activations,resynth:~resynth,components:n_components,resynthMode: 1).wait;
	{
		~bases.plot("bases");
		~activations.plot("activations");
	}.defer;
}.play;
)

// base as a filter
(
Routine{
	var drums = Buffer.read(s,FluidFilesPath("Nicol-LoopE-M.wav"));
	var voice = Buffer.read(s,FluidFilesPath("Tremblay-AaS-VoiceQC-B2K-M.wav"));
	var song = Buffer.read(s,FluidFilesPath("Tremblay-BeatRemember.wav"));
	s.sync;

	"drums through the drums bases as filters".postln;
	{
		var src = PlayBuf.ar(1,drums,BufRateScale.ir(drums),doneAction:2);
		var sig = FluidNMFFilter.ar(src,~bases,2);
		sig;
	}.play;

	(drums.duration+1).wait;

	"voice through the drum bases as filters".postln;
	{
		var src = PlayBuf.ar(1,voice,BufRateScale.ir(voice),doneAction:2);
		var sig = FluidNMFFilter.ar(src,~bases,2);
		sig;
	}.play;

	(voice.duration+1).wait;

	"song through the drum bases as filters".postln;
	{
		var src = PlayBuf.ar(2,song,BufRateScale.ir(song),doneAction:2)[0];
		var sig = FluidNMFFilter.ar(src,~bases,2);
		sig;
	}.play;
}.play;
)

// activations as an envelope
(
{
	var activation = PlayBuf.ar(2,~activations,BufRateScale.ir(~activations),doneAction:2);
	var sig = WhiteNoise.ar(0.dbamp) * activation;
	sig;
}.play;
)

// put them together...
(
{
	var activation = PlayBuf.ar(2,~activations,BufRateScale.ir(~activations),doneAction:2);
	var sig = WhiteNoise.ar(0.dbamp);
	sig = FluidNMFFilter.ar(sig,~bases,2) * activation;
	sig;
}.play;
)

// as a matcher, train on only 4 of the 22 seconds

(
Task{
	var dog = Buffer.readChannel(s,FluidFilesPath("Tremblay-BaB-SoundscapeGolcarWithDog.wav"),channels:[0]);
	var bases = Buffer(s);
	var match = [0,0];
	var win = Window("FluidNMFMatch",Rect(0,0,200,400));
	var uv = UserView(win,win.bounds)
	.drawFunc_{
		var w = uv.bounds.width / 2;
		Pen.color_(Color.green);
		match.do{
			arg match_val, i;
			var match_norm = match_val.linlin(0,30,0,uv.bounds.height);
			var top = uv.bounds.height - match_norm;
			/*top.postln;*/
			Pen.addRect(Rect(i * w,top,w,match_norm));
			Pen.draw;
		};
	};

	OSCdef(\nmfmatch,{
		arg msg;
		match = msg[3..];
		{uv.refresh}.defer;
	},"/nmfmatch");

	win.front;

	s.sync;

	FluidBufNMF.process(s,dog,numFrames:dog.sampleRate * 4,bases:bases,components:2).wait;
	{
		var sig = PlayBuf.ar(1,dog,BufRateScale.ir(dog),doneAction:2);
		SendReply.kr(Impulse.kr(30),"/nmfmatch",FluidNMFMatch.kr(sig,bases,2));
		sig;
	}.play;
}.play(AppClock);
)
