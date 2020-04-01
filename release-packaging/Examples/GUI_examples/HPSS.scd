(
var win, soundFileView, freqSscope,loadButton, loopButton;
var harmSlider, percSlider, mixSlider;
var soundFile, buffer;
var synthDef, synth;
var makeSynthDef;

Font.default = Font("Monaco", 16);
buffer = Buffer.new;
win = Window.new("HPSS", Rect(200,200,800,450)).background_(Color.gray);

soundFileView = SoundFileView.new(win)
	.gridOn_(false)
	.waveColors_([Color.white]);

loadButton = Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_([["Load", Color.grey, Color.grey(0.8)]]);

loopButton =  Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_(
				[["Play", Color.grey, Color.grey(0.8)],
				["Stop", Color.grey, Color.grey(0.2)]]
			);

harmSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
percSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
mixSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
freqSscope = FreqScopeView(win, server:Server.default);
freqSscope.active_(true);

loadButton.action_{
	FileDialog({ |path|
		soundFile = SoundFile.new;
		soundFile.openRead(path[0]);
		buffer = Buffer.read(Server.default, path[0]);
		soundFileView.soundfile = soundFile;
		soundFileView.read(0, soundFile.numFrames);
   });
};

loopButton.action_{|but|
	if(but.value == 1, {
		synth = Synth(\hpssExtractionDemo, [\buffer, buffer.bufnum]);
		mixSlider.action.value(mixSlider);
	},{
		synth.free;
	});
};


mixSlider.action_{|slider|
	synth.set(\bal, ControlSpec(0, 1).map(slider.value));
};



makeSynthDef = {

synthDef  = SynthDef(\hpssExtractionDemo,
	{|buffer,  bal = 0.5|
		var player, fhpss, mix;
		var harmSize = (2 * ControlSpec(1, 100, step:1).map(harmSlider.value)) - 1;
		var percSize = (2 * ControlSpec(1,100, step:1).map(percSlider.value)) - 1;
		player = PlayBuf.ar(1, buffer, loop:1);
		fhpss = FluidHPSS.ar(in: player, harmFilterSize: harmSize, percFilterSize: percSize, maskingMode: 1, harmThreshFreq1: 0.1, harmThreshAmp1: 0, harmThreshFreq2: 0.5, harmThreshAmp2: 0, percThreshFreq1: 0.1, percThreshAmp1: 0, percThreshFreq2: 0.5, percThreshAmp2: 0, windowSize: 1024, hopSize: 256, fftSize: -1);

		mix =(bal * fhpss[0]) + ((1 - bal) * fhpss[1]);
			Out.ar(0,Pan2.ar(mix));
	}
).add;

};

win.layout_(
	VLayout(
		[
			HLayout(
				[loadButton, stretch:1],
				[soundFileView,   stretch:5]
				), stretch:2
		],
		[
			HLayout(
				[loopButton, stretch:1],
				[VLayout(
					HLayout(StaticText(win).string_("H  Size ").minWidth_(100), harmSlider),
					HLayout(StaticText(win).string_("P Size").minWidth_(100), percSlider),
					HLayout(StaticText(win).string_("Mix").minWidth_(100), mixSlider)
					), stretch:5]
				), stretch:2
		],
		[freqSscope, stretch:2]
	)
);

makeSynthDef.value;
win.front;
)