(
var server;
var win, soundFileView,loadButton, processButton;
var ksSlider, thSlider;
var soundFile, audioBuffer, slicesBuffer, slicesArray;
var addSelections, playFunc, stopFunc;
var synthDef, synth;
var synths;

var playing, currentSelection, colors, prevColor;

playing = Array.fill(10, {false});
server = Server.default;
Font.default = Font("Monaco", 16);

audioBuffer = Buffer.new;
slicesBuffer = Buffer.new;

colors = Array.fill(64, {Color.rand});
synths = Array.fill(10, {nil});

synthDef = SynthDef(\noveltySegDemo,{|buf, start, end|
	Out.ar(0, BufRd.ar(1, buf, Phasor.ar(1,  1, start, end)));
}).add;

playFunc = {|index|
	var dur;
	currentSelection = index;
	if(playing[index].not){
		synths[index] = Synth(\noveltySegDemo,
			[\buf, audioBuffer.bufnum,
			\start, slicesArray[index],
			\end, slicesArray[index+1]
		     ]);
		playing[index] = true;
	};
	soundFileView.setSelectionColor(currentSelection, Color.white);
};

stopFunc = {|index| synths[index].free; playing[index] = false;
			soundFileView.setSelectionColor(
		         index, colors[index]
	         );
};


win = Window.new("NoveltySegmentation",
	Rect(200,200,800,450)).background_(Color.gray);

win.view.keyDownAction_{|view, char, modifiers, unicode, keycode, key|
	if (char.isDecDigit){
		var digit = char.digit;
		if(digit.notNil && slicesArray.notNil){
			playFunc.value(digit - 1);
		}
	}
};

win.view.keyUpAction_{|view, char|
	if(char.isDecDigit){
		stopFunc.value(char.digit - 1);
    }
};

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

ksSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);
thSlider = Slider(win, Rect(0, 0, 100, 10)).value_(0.5);


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
	var ks = 2*(ControlSpec(2, 100, step:1).map(ksSlider.value)) - 1;
	var th = ControlSpec(0, 1).map(thSlider.value);
	if(but.value == 1, {
		Routine{
		 FluidBufNoveltySlice.process(
			server,
			source:audioBuffer.bufnum,
			indices:slicesBuffer.bufnum,
			kernelSize:ks,
			threshold: th
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
					HLayout(StaticText(win).string_("Kernel ").minWidth_(100), ksSlider),
					HLayout(StaticText(win).string_(" Threshold").minWidth_(100), thSlider)
					), stretch:5]
				), stretch:2
		]
	)
);

win.front;
)
