(
var win, soundFileView, freqSscope,loadButton, loopButton;
var thresholdSlider, lenSlider, mixSlider;
var soundFile, buffer;
var synthDef, synth;

Font.default = Font("Monaco", 16);
buffer = Buffer.new;
win = Window.new("SineExtraction",
	Rect(200,200,800,450)).background_(Color.gray);

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

thresholdSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
lenSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
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
		synth = Synth(\sineExtractionDemo, [\buffer, buffer.bufnum]);
		mixSlider.action.value(mixSlider);
		thresholdSlider.action.value(thresholdSlider);
		lenSlider.action.value(lenSlider);
	},{
		synth.free;
	});
};


mixSlider.action_{|slider|
	synth.set(\bal, ControlSpec(0, 1).map(slider.value));
};


thresholdSlider.action_{|slider|
	synth.set(\threshold, ControlSpec(-144, 0).map(slider.value));
};


lenSlider.action_{|slider|
	synth.set(\minLength, ControlSpec(0, 30).map(slider.value));
};


synthDef  = SynthDef(\sineExtractionDemo,
	{|buffer, threshold = 0.9, minLength = 15, bal = 0.5|
		var player, fse, mix;
		player = PlayBuf.ar(1, buffer, loop:1);
		fse = FluidSines.ar(in: player, bandwidth: 76,
			detectionThreshold: threshold, minTrackLen: minLength,
			 windowSize: 2048,
			 hopSize: 512, fftSize: 8192
		);
		mix =(bal * fse[0]) + ((1 - bal) * fse[1]);
		Out.ar(0,Pan2.ar(mix));
	}
).add;

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
					HLayout(StaticText(win).string_("Threshold ").minWidth_(100), thresholdSlider),
					HLayout(StaticText(win).string_("Min Length").minWidth_(100), lenSlider),
					HLayout(StaticText(win).string_("Mix").minWidth_(100), mixSlider)
					), stretch:5]
				), stretch:2
		],
		[freqSscope, stretch:2]
	)
);

win.front;
)
