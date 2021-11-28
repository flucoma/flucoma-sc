(
~counter = 0;
~predicting = false;
~prediction_buf = Buffer.alloc(s,10);
Window.closeAll;
~win = Window("MLP Regressor",Rect(0,0,1000,400));
~multisliderview = MultiSliderView(~win,Rect(0,0,400,400))
.size_(10)
.elasticMode_(true)
.action_({
	arg msv;
	// ~synth.set(\val,msv.value);
	// msv.value.postln;
	~y_buf.setn(0,msv.value);
});

Slider2D(~win,Rect(400,0,400,400))
.action_({
	arg s2d;
	[s2d.x,s2d.y].postln;
	~x_buf.setn(0,[s2d.x,s2d.y]);

	if(~predicting,{
		~nn.predictPoint(~x_buf,~y_buf,{
			~y_buf.getn(0,10,{
				{~multisliderview.value_(prediction_values)}.defer;
			});
		});
	});
});

Button(~win,Rect(800,0,200,20))
.states_([["Add Point"]])
.action_({
	arg but;
	var id = "example-%".format(~counter);
	~ds_input.addPoint(id,~x_buf);
	~ds_output.addPoint(id,~y_buf);
	~counter = ~counter + 1;

	~ds_input.print;
	~ds_output.print;
});

Button(~win,Rect(800,20,200,20))
.states_([["Train"]])
.action_({
	arg but;
	~nn.fit(~ds_input,~ds_output,{
		arg loss;
		"loss: %".format(loss).postln;
	});
});

Button(~win,Rect(800,40,200,20))
.states_([["Not Predicting",Color.yellow,Color.black],["Is Predicting",Color.green,Color.black]])
.action_({
	arg but;
	~predicting = but.value.asBoolean;
});

~win.front;

~ds_input = FluidDataSet(s);
~ds_output = FluidDataSet(s);
~x_buf = Buffer.alloc(s,2);
~y_buf = Buffer.alloc(s,10);
~nn = FluidMLPRegressor(s,[7],FluidMLPRegressor.sigmoid,FluidMLPRegressor.sigmoid,learnRate:0.1,batchSize:1,validation:0);

~synth = {
	//arg val = #[0,0,0,0,0,0,0,0,0,0];
	var val = FluidBufToKr.kr(~y_buf)
	var osc1, osc2, feed1, feed2, base1=69, base2=69, base3 = 130;
	#feed2,feed1 = LocalIn.ar(2);
	osc1 = MoogFF.ar(SinOsc.ar((((feed1 * val[0]) +  val[1]) * base1).midicps,mul: (val[2] * 50).dbamp).atan,(base3 - (val[3] * (FluidLoudness.kr(feed2, 1, 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[4] * 3.5);
	osc2 = MoogFF.ar(SinOsc.ar((((feed2 * val[5]) +  val[6]) * base2).midicps,mul: (val[7] * 50).dbamp).atan,(base3 - (val[8] * (FluidLoudness.kr(feed1, 1, 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[9] * 3.5);
	Out.ar(0,LeakDC.ar([osc1,osc2],mul: 0.1));
	LocalOut.ar([osc1,osc2]);
}.play;
)