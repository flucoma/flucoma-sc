//1- make the gui then the synth below
(
var trained = 0, entering = 0;
var va = Array.fill(10,{0.5});
var input = Buffer.alloc(s,2);
var output = Buffer.alloc(s,10);
var mlp = FluidMLPRegressor(s,[8, 6, 4],1,1000,0.01);
var entry = 0;

~inData = FluidDataSet(s,\indata);
~outData = FluidDataSet(s,\outdata);

w = Window("ChaosSynth", Rect(10, 10, 790, 320)).front;
a = MultiSliderView(w,Rect(10, 10, 400, 300)).elasticMode_(1).isFilled_(1);
a.value=va;
a.action = {arg q;
	b.set(\val, q.value);
	va = q.value;};
f = Slider2D(w,Rect(420,10,300, 300));
f.x = 0.5;
f.y = 0.5;
f.action = {arg x,y; //if trained, predict the point f.x f.y
	if (trained == 1, {
		input.setn(0, [f.x, f.y]);
		mlp.predictPoint(input,output,0,{
			output.getn(0,10,{
				|x|va = x; b.set(\val, va); {a.value = va;}.defer;});
		});
	});
	if (entering == 1, { //if entering a point, add to the the database f.x f.y against the array va
		input.setn(0, [f.x, f.y]);
		output.setn(0, va);
		~inData.addPoint(entry.asSymbol,input);
		~outData.addPoint(entry.asSymbol,output);
		entering = 0;
		entry = entry + 1;
		{d.value = 0;}.defer;
	});
};

c = Button(w, Rect(730,200,50, 20)).states_([["train", Color.red, Color.white], ["trained", Color.white, Color.grey]]).action_{
	mlp.fit(~inData,~outData,{|x|
		trained = 1;
		c.value = 1;
		e.value = x.round(0.001).asString;
	});//train the network
};
d = Button(w, Rect(730,10,50, 20)).states_([["entry", Color.white, Color.grey], ["entry",  Color.red, Color.white]]).action_{
	entering = 1;
};
e = TextField(w,Rect(730,250,50,20)).string_(0.asString);
)

//2- the synth (CAREFUL - LOUD!)
(
b = {
	arg val = #[0,0,0,0,0,0,0,0,0,0];
	var osc1, osc2, feed1, feed2, base1=69, base2=69, base3 = 130;
	#feed2,feed1 = LocalIn.ar(2);
	osc1 = MoogFF.ar(SinOsc.ar((((feed1 * val[0]) +  val[1]) * base1).midicps,mul: (val[2] * 50).dbamp).atan,(base3 - (val[3] * FluidLoudness.kr(feed2, 1, 0, hopSize: 64)[0].clip(-120,0))).lag(128/44100).midicps, val[4] * 4);
	osc2 = MoogFF.ar(SinOsc.ar((((feed2 * val[5]) +  val[6]) * base2).midicps,mul: (val[7] * 50).dbamp).atan,(base3 - (val[8] * FluidLoudness.kr(feed1, 1, 0, hopSize: 64)[0].clip(-120,0))).lag(128/44100).midicps, val[9] * 4);
	Out.ar(0,LeakDC.ar([osc1,osc2]));
	LocalOut.ar([osc1,osc2]);
}.play;
)

~inData.print;
~outData.print;

/////////
//3 - play with the multislider
//4 - when you like a spot, click entry (become read) then a position in the 2D graph where this point should be
//5 - do that for a few points
//6 - click train
//7 - the 2D graph controls the 10D
//8 - if you like a new sound and you want to update the graph, just click entry, then where it should be in the 2D, then retrain when you are happy
