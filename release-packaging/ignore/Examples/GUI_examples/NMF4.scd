(
var server;
var win, soundFileView, loadButton, loopButton;
var sliders;
var soundFile, audioBuffer, destBuffer;
var synthDef, synth;
var sl1, sl2, sl3, sl4;

server = Server.default;
Font.default = Font("Monaco", 16);

audioBuffer = Buffer.new;
destBuffer = Buffer.new;


synthDef = SynthDef(\nmfDemo,{|bufnum, a1, a2, a3, a4|
	var p = PlayBuf.ar(4, bufnum, loop:1);
	var mix = (a1*p[0]) + (a2 * p[1]) + (a3*p[2]) + (a4*p[3]);
	Out.ar(0, Pan2.ar(mix));
}).add;



win = Window.new("NMF4",
	Rect(200,200,800,450)).background_(Color.gray);

soundFileView = SoundFileView.new(win)
	.gridOn_(false)
	.waveColors_([Color.white]);

loadButton = Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_([["Load", Color.grey, Color.grey(0.8)],
	                      ["Wait", Color.grey, Color.grey(0.2)]]
	);

loopButton =  Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_(
				[["Play", Color.grey, Color.grey(0.8)],
				["Stop", Color.grey, Color.grey(0.2)]]
			);

sliders = Array.fill(4, {|i|
	var s = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
	s.action_{
		var sym = ("a"++(i+1)).asSymbol;
		synth.set(sym, ControlSpec(0, 1).map(s.value));
	}
});


loadButton.action_{
	FileDialog({ |path|
		path.postln;

		soundFile = SoundFile.new;
		soundFile.openRead(path[0]);

		soundFileView.soundfile = soundFile;
		soundFileView.read(0, soundFile.numFrames);
		Routine{
		audioBuffer = Buffer.read(server, path[0]);
		server.sync;
		FluidBufNMF.process(server,
			audioBuffer.bufnum,destination:destBuffer.bufnum, components:4
		);
		server.sync;
		destBuffer.query;
		server.sync;
		{loadButton.value_(0)}.defer;
		}.play;
   });
};

loopButton.action_{|but|
	var a1 = ControlSpec(0, 1).map(sliders[0].value);
	var a2 = ControlSpec(0, 1).map(sliders[1].value);
	var a3 = ControlSpec(0, 1).map(sliders[2].value);
	var a4 = ControlSpec(0, 1).map(sliders[3].value);

	if(but.value == 1, {
		synth = Synth(\nmfDemo,
			[\bufnum, destBuffer.bufnum, \a1, a1, \a2, a2, \a3, a3, \a4, a4]);
	},{
		synth.free;
	});
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
					HLayout(StaticText(win).string_("source 1 ").minWidth_(100), sliders[0]),
					HLayout(StaticText(win).string_("source 2 ").minWidth_(100), sliders[1]),
					HLayout(StaticText(win).string_("source 3 ").minWidth_(100), sliders[2]),
					HLayout(StaticText(win).string_("source 4 ").minWidth_(100), sliders[3])
				), stretch:5]
				), stretch:2
		]
	)
);

win.front;
)
