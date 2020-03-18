(
var win, soundFileView, freqSscope,loadButton, loopButton;
var fwSlider, bwSlider, mixSlider;
var soundFile, buffer;
var synthDef, synth;

Font.default = Font("Monaco", 16);
buffer = Buffer.new;
win = Window.new("TransientExtraction",
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

fwSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
bwSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
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
		synth = Synth(\transientExtractionDemo, [\buffer, buffer.bufnum]);
		mixSlider.action.value(mixSlider);
		fwSlider.action.value(fwSlider);
		bwSlider.action.value(bwSlider);
	},{
		synth.free;
	});
};


mixSlider.action_{|slider|
	synth.set(\bal, ControlSpec(0, 1).map(slider.value));
};


fwSlider.action_{|slider|
	synth.set(\fw, ControlSpec(0.0001, 3, \exp).map(slider.value));
};


bwSlider.action_{|slider|
	synth.set(\bw, ControlSpec(0.0001, 3, \exp).map(slider.value));
};


synthDef  = SynthDef(\transientExtractionDemo,
	{|buffer, fw = 3, bw = 1, bal = 0.5|
		var player, fte, mix;
		player = PlayBuf.ar(1, buffer, loop:1);
		fte = FluidTransients.ar(in: player, threshFwd:fw, threshBack:bw, clumpLength:256);
		mix =(bal * fte[0]) + ((1 - bal) * fte[1]);
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
					HLayout(StaticText(win).string_("Forward Th ").minWidth_(100), fwSlider),
					HLayout(StaticText(win).string_("Backward Th").minWidth_(100), bwSlider),
					HLayout(StaticText(win).string_("Mix").minWidth_(100), mixSlider)
					), stretch:5]
				), stretch:2
		],
		[freqSscope, stretch:2]
	)
);

win.front;
)
