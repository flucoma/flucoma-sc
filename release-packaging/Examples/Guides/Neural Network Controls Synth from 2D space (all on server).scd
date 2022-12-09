(
s.waitForBoot{
	// a counter that will increment each time we add a point to the datasets
	// (so that they each can have a unique identifier)
	~counter = 0;

	~ds_input = FluidDataSet(s); // dataset to hold the input data points (xy position)
	~ds_output = FluidDataSet(s); // data set to hold the output data points (the 10 synth parameters)
	~x_buf = Buffer.alloc(s,2); // a buffer for holding the current xy position (2 dimensions)
	~y_buf = Buffer.alloc(s,10); // a buffer for holding the current synthparameters (10 parameters)

	// the neural network. for more info on these arguments, visit learn.flucoma.com/reference/mlpregressor
	~nn = FluidMLPRegressor(s,[7],FluidMLPRegressor.sigmoid,FluidMLPRegressor.sigmoid,learnRate:0.1,batchSize:1,validation:0);

	// just nice to close any open windows, in case this script gets run multiple times...
	// that way the windows don't pile up
	Window.closeAll;

	~win = Window("MLP Regressor",Rect(0,0,1000,400));

	Slider2D(~win,Rect(0,0,400,400))
	.action_({
		arg s2d;
		// [s2d.x,s2d.y].postln;

		// we're sendinig these values up to the synth, once there, they will get written into the buffer
		// for the mlp to use as input
		~synth.set(\x,s2d.x,\y,s2d.y);
	});

	~multisliderview = MultiSliderView(~win,Rect(400,0,400,400))
	.size_(10) // we know that it will need 10 sliders
	.elasticMode_(true) // this will ensure that the sliders are spread out evenly across the whole view
	.action_({
		arg msv;

		// here we'll just set these values directly into the buffer
		// on the server they get read out of the buffer and used to control the synthesizer
		~y_buf.setn(0,msv.value);
	});

	// a button for adding points to the datasets, both datasets at the same time
	// with the same identifier
	Button(~win,Rect(800,0,200,20))
	.states_([["Add Point"]])
	.action_({
		arg but;
		var id = "example-%".format(~counter); // use the counter to create a unique identifier
		~ds_input.addPoint(id,~x_buf); // add a point to the input dataset using whatever values are in x_buf
		~ds_output.addPoint(id,~y_buf); // add a pointi to the output dataset using whatever values a are in y_buf
		~counter = ~counter + 1; // increment the counter!

		// nice to just see every time what is going into the datasets
		~ds_input.print;
		~ds_output.print;
	});

	// a button to train train the neural network. you can push the button multiple times to watch the loss
	// decrease. each time you press it, the neural network doesn't reset, it just keeps training from where it left off
	Button(~win,Rect(800,20,200,20))
	.states_([["Train"]])
	.action_({
		arg but;
		~nn.fit(~ds_input,~ds_output,{ // provide the dataset to use as input and the dataset to use os output
			arg loss;
			"loss: %".format(loss).postln; // post the loss so we can watch it go down after multiple trainings
		});
	});

	// a button to control when the neural network is actually making predictions
	// we want it to *not* be making predictions while we're adding points to the datasets (because we want
	// the neural network to not be writing into y_buf)
	Button(~win,Rect(800,40,200,20))
	.states_([["Not Predicting",Color.yellow,Color.black],["Is Predicting",Color.green,Color.black]])
	.action_({
		arg but;
		~synth.set(\predicting,but.value); // send the "boolean" (0 or 1) up to the synth
	});

	~win.front;

	~synth = {
		arg predicting = 0, x = 0, y = 0;
		var osc1, osc2, feed1, feed2, base1=69, base2=69, base3 = 130, val, trig;

		FluidKrToBuf.kr([x,y],~x_buf); // receive the xy positions as arguments to the synth, then write them into the buffer here

		// if predicting is 1 "trig" will be impulses 30 times per second, if 0 it will be just a stream of zeros
		trig = Impulse.kr(30) * predicting;

		// the neural network will make a prediction each time a trigger, or impulse, is received in the first argument
		// the next two arguments are (1) which buffer to use as input to the neural network, and (2) which buffer
		// to write the output prediction into
		~nn.kr(trig,~x_buf,~y_buf);

		// read the 10 synth parameter values out of this buffer. val is a control rate stream of the 10 values
		// when the neural network is making predictions (predicting == 1), it will be writing the predictions
		// into that buffer, so that is what will be read out of here. when the neural network is not making predictions
		// (predicting == 0) it will not be writing values into the buffer, so you can use the MultiSliderView above to
		// write values into the buffer -- they'll still get read out into a control stream right here to control the synth!
		val = FluidBufToKr.kr(~y_buf);

		// if we are making predictions (trig is a series of impulses), send the values back to the language so that we can
		// update the values in the multislider. this is basically only for aesthetic purposes. it's nice to see the multislider
		// wiggle as the neural network makes it's predictions!
		SendReply.kr(trig,"/predictions",val);

		// the actual synthesis algorithm, made by P.A. Tremblay
		#feed2,feed1 = LocalIn.ar(2);
		osc1 = MoogFF.ar(SinOsc.ar((((feed1 * val[0]) +  val[1]) * base1).midicps,mul: (val[2] * 50).dbamp).atan,(base3 - (val[3] * (FluidLoudness.kr(feed2,truePeak: 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[4] * 3.5);
		osc2 = MoogFF.ar(SinOsc.ar((((feed2 * val[5]) +  val[6]) * base2).midicps,mul: (val[7] * 50).dbamp).atan,(base3 - (val[8] * (FluidLoudness.kr(feed1,truePeak: 0, hopSize: 64)[0].clip(-120,0) + 120))).lag(128/44100).midicps, val[9] * 3.5);
		Out.ar(0,LeakDC.ar([osc1,osc2],mul: 0.1));
		LocalOut.ar([osc1,osc2]);
	}.play;

	// catch the osc messages sent by the SendReply above and update the MultiSliderView
	OSCdef(\predictions,{
		arg msg;
		// msg.postln;
		{~multisliderview.value_(msg[3..])}.defer;
	},"/predictions");
}
)