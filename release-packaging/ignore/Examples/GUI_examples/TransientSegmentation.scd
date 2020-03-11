(
var server;
var win, soundFileView,loadButton, processButton;
var fwSlider, bwSlider, debounceSlider;
var soundFile, audioBuffer, slicesBuffer, slicesArray;
var addSelections, playFunc, stopFunc;
var synthDef, synth;

var playing, currentSelection, colors, prevColor;

playing = false;
server = Server.default;
Font.default = Font("Monaco", 16);

audioBuffer = Buffer.new;
slicesBuffer = Buffer.new;

colors = Array.fill(64, {Color.rand});

synthDef = SynthDef(\transientSegDemo,{|buf, start, end|
	Out.ar(0, BufRd.ar(1, buf, Phasor.ar(1,  1, start, end)));
}).add;

playFunc = {|index|
	var dur;
	currentSelection = index;
	if(playing.not){
	synth = Synth(\transientSegDemo,
		[\buf, audioBuffer.bufnum,
			\start, slicesArray[index],
			\end, slicesArray[index+1]
	]);
		playing = true;
	};
	soundFileView.setSelectionColor(currentSelection, Color.white);
};

stopFunc = {synth.free; playing = false;
			soundFileView.setSelectionColor(currentSelection, colors[currentSelection]);

};


win = Window.new("TransientSegmentation",
	Rect(200,200,800,450)).background_(Color.gray);

win.view.keyDownAction_{|view, char, modifiers, unicode, keycode, key|
	if (char.isDecDigit){
		var digit = char.digit;
		if(slicesArray.notNil){
			playFunc.value(digit - 1);
		}
	}
};

win.view.keyUpAction_{stopFunc.value;};



soundFileView = SoundFileView.new(win)
	.gridOn_(false)
	.waveColors_([Color.white]);

loadButton = Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_([["Load", Color.grey, Color.grey(0.8)]]);

processButton =  Button(win, Rect(0, 0, 100, 100))
			.minHeight_(150)
			.states_(
				[["Process", Color.grey, Color.grey(0.8)],
				["Wait", Color.grey, Color.grey(0.2)]]
			);

fwSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
bwSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
debounceSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);

loadButton.action_{
	FileDialog({ |path|
		path.postln;
		soundFile = SoundFile.new;
		soundFile.openRead(path[0]);
		audioBuffer = Buffer.read(server, path[0]);
		soundFileView.soundfile = soundFile;
		soundFileView.read(0, soundFile.numFrames);
   });
};

processButton.action_{|but|
	var fw = ControlSpec(0, 5).map(fwSlider.value);
	var bw = ControlSpec(0, 5).map(bwSlider.value);
	var db = ControlSpec(1, 4410).map(debounceSlider.value);
	if(but.value == 1, {
		Routine{
		 FluidBufTransientSlice.process(
			server,
			source:audioBuffer.bufnum,
			indices:slicesBuffer.bufnum,
			threshFwd: fw,
			threshBack: bw,
			clumpLength:db
		  );
		server.sync;
		slicesBuffer.loadToFloatArray(action:{|arr|
				slicesArray = arr;
				{  processButton.value_(0);
					addSelections.value(slicesArray)
				}.defer;

			});
		}.play;
	});
};



addSelections = {|array|
	var nSegments = min(array.size, soundFileView.selections.size) - 1;
	soundFileView.selections.do({|sel, i|  soundFileView.selectNone(i)});
	nSegments.do({|i|
		soundFileView.setSelectionStart(i, array[i]);
		soundFileView.setSelectionSize(i, array[i+1] - array[i]);
		soundFileView.setSelectionColor(i, colors[i]);
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
				[processButton, stretch:1],
				[VLayout(
					HLayout(StaticText(win).string_("Forward Th ").minWidth_(100), fwSlider),
					HLayout(StaticText(win).string_("Backward Th").minWidth_(100), bwSlider),
					HLayout(StaticText(win).string_("Debounce").minWidth_(100), debounceSlider)
					), stretch:5]
				), stretch:2
		]
	)
);

win.front;
)
