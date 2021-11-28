//1- make the gui then the synth below
(
Window.closeAll;
s.waitForBoot{
	Task{
		var trained = 0, entering = 0;
		var input_buffer = Buffer.alloc(s,2);
		var output_buffer = Buffer.alloc(s,10);
		var mlp = FluidMLPRegressor(s,[6],activation: 1,outputActivation: 1,maxIter: 1000,learnRate: 0.1,momentum: 0.9,batchSize: 1);
		var entry_counter = 0;
		var win, multislider, xyslider, synth, error_st, prediction_but, addPoints_but, train_but;
		var item_width = 100;
		var inData = FluidDataSet(s);
		var outData = FluidDataSet(s);

		win = Window("ChaosSynth", Rect(10, 10, 840, 320)).front;

		multislider = MultiSliderView(win,Rect(10, 10, 400, 300))
		.elasticMode_(1)
		.isFilled_(1)
		.action_({
			arg ms;
			// ms.value.postln;
			synth.set(\val,ms.value);
			output_buffer.setn(0,ms.value);
		})
		.value_(0.5.dup(10));

		xyslider = Slider2D(win,Rect(420,10,300, 300))
		.x_(0.5)
		.y_(0.5)
		.action_({
			arg sl;

			input_buffer.setn(0,[sl.x,sl.y]);

			if(prediction_but.value.asBoolean,{
				mlp.predictPoint(input_buffer,output_buffer,{
					output_buffer.getn(0,10,{
						arg output_values;
						synth.set(\val, output_values);
						{
							multislider.value_(output_values)
						}.defer;
					});
				});
			});
		});

		addPoints_but = Button(win, Rect(730,10,item_width, 20))
		.states_([["add points", Color.white, Color.grey]])
		.action_({
			inData.addPoint(entry_counter.asSymbol,input_buffer);
			outData.addPoint(entry_counter.asSymbol,output_buffer);
			entry_counter = entry_counter + 1;
			inData.print;
			outData.print;
		});

		train_but = Button(win, Rect(730,240,item_width, 20))
		.states_([["train", Color.red, Color.white]])
		.action_({
			mlp.fit(inData,outData,{
				arg loss;
				{error_st.string_("loss: %".format(loss.round(0.001)))}.defer;
			});
		});

		prediction_but = Button(win, Rect(730,40,item_width, 20))
		.states_([["Not Predicting", Color.black, Color.white],["Predicting",Color.black,Color.white]]);

		error_st = StaticText(win,Rect(732,260,item_width,20)).string_("Error:");
		StaticText(win,Rect(732,70,item_width,20)).string_("rate:");
		TextField(win,Rect(730,90,item_width,20)).string_(0.1.asString).action_{|in|mlp.learnRate = in.value.asFloat.postln;};
		StaticText(win,Rect(732,110,item_width,20)).string_("momentum:");
		TextField(win,Rect(730,130,item_width,20)).string_(0.9.asString).action_{|in|mlp.momentum = in.value.asFloat.postln;};
		StaticText(win,Rect(732,150,item_width,20)).string_("maxIter:");
		TextField(win,Rect(730,170,item_width,20)).string_(1000.asString).action_{|in| mlp.maxIter = in.value.asInteger.postln;};

		s.sync;

		//2- the synth
		synth = {
			arg val = #[0,0,0,0,0,0,0,0,0,0];
			var osc1, osc2, feed1, feed2, base1=69, base2=69, base3 = 130;
			#feed2,feed1 = LocalIn.ar(2);
			osc1 = MoogFF.ar(SinOsc.ar((((feed1 * val[0]) +  val[1]) * base1).midicps,mul: (val[2] * 50).dbamp).atan,(base3 - (val[3] * (FluidLoudness.kr(feed2, 1, 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[4] * 3.5);
			osc2 = MoogFF.ar(SinOsc.ar((((feed2 * val[5]) +  val[6]) * base2).midicps,mul: (val[7] * 50).dbamp).atan,(base3 - (val[8] * (FluidLoudness.kr(feed1, 1, 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[9] * 3.5);
			Out.ar(0,LeakDC.ar([osc1,osc2],mul: 0.1));
			LocalOut.ar([osc1,osc2]);
		}.play;
	}.play(AppClock);
};
)

/////////
//3 - play with the multislider
//4 - when you like a spot, move the 2d slider to a position that you want to represent that sound and click "add point"
//5 - do that for a few points
//6 - click train, keep clicking train until the loss is at or below 0.01 or so. feel free to adjust the learning rate, momentum, and max iter.
//7 - the 2D graph controls the 10D
