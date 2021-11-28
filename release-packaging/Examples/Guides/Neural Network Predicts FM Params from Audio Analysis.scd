(
Window.closeAll;
s.options.inDevice_("MacBook Pro Microphone");
s.options.outDevice_("External Headphones");
// s.options.sampleRate_(48000);
s.options.sampleRate_(44100);
s.waitForBoot{
	Task{
		var win = Window(bounds:Rect(100,100,1000,800));
		var label_width = 120;
		var item_width = 300;
		var mfcc_multslider;
		var nMFCCs = 13;
		var mfccbuf = Buffer.alloc(s,nMFCCs);
		var parambuf = Buffer.alloc(s,3);
		var id_counter = 0;
		var continuous_training = false;
		var mfcc_ds = FluidDataSet(s);
		var param_ds = FluidDataSet(s);
		var mfcc_ds_norm = FluidDataSet(s);
		var param_ds_norm = FluidDataSet(s);
		var scaler_params = FluidNormalize(s);
		var scaler_mfcc = FluidNormalize(s);
		var nn = FluidMLPRegressor(s,[3,3],FluidMLPRegressor.sigmoid,FluidMLPRegressor.sigmoid,learnRate:0.05,batchSize:5,validation:0);
		var synth, loss_st;
		var param_sliders = Array.newClear(3);
		var statsWinSl, hidden_tf, batchSize_nb, momentum_nb, learnRate_nb, maxIter_nb, outAct_pum, act_pum;
		var add_point = {
			var id = "point-%".format(id_counter);
			mfcc_ds.addPoint(id,mfccbuf,{mfcc_ds.print});
			param_ds.addPoint(id,parambuf,{param_ds.print});
			id_counter = id_counter + 1;
		};
		var train = {
			scaler_mfcc.fitTransform(mfcc_ds,mfcc_ds_norm,{
				scaler_params.fitTransform(param_ds,param_ds_norm,{
					// mfcc_ds.print;
					// param_ds.print;
					nn.fit(mfcc_ds_norm,param_ds_norm,{
						arg loss;
						// loss.postln;
						defer{loss_st.string_("loss: %".format(loss))};
						if(continuous_training,{
							train.value;
						});
					});
				});
			});
		};
		var open_mlp = {
			arg path;
			// nn.prGetParams.postln;
			nn.read(path,{
				var params = nn.prGetParams;
				var n_layers = params[1];
				var layers_string = "";

				// params.postln;

				n_layers.do({
					arg i;
					if(i > 0,{layers_string = "% ".format(layers_string)});
					layers_string = "%%".format(layers_string,params[2+i]);
				});

				nn.maxIter_(maxIter_nb.value);
				nn.learnRate_(learnRate_nb.value);
				nn.momentum_(momentum_nb.value);
				nn.batchSize_(batchSize_nb.value);

				defer{
					hidden_tf.string_(layers_string);
					act_pum.value_(nn.activation);
					outAct_pum.value_(nn.outputActivation);
					/*					maxIter_nb.value_(nn.maxIter);
					learnRate_nb.value_(nn.learnRate);
					momentum_nb.value_(nn.momentum);
					batchSize_nb.value_(nn.batchSize);*/
				};
			});
		};

		s.sync;

		synth = {
			arg vol = -15, isPredicting = 0, avg_win = 0, smooth_params = 0;
			var params = FluidStats.kr(FluidBufToKr.kr(parambuf),ControlRate.ir * smooth_params * isPredicting)[0];
			var msig = SinOsc.ar(params[1],0,params[2] * params[1]);
			var csig = SinOsc.ar(params[0] + msig);
			var sound_in = SoundIn.ar(0);
			var analysis_sig, mfccs, trig, mfccbuf_norm, parambuf_norm;

			csig = BLowPass4.ar(csig,16000);
			csig = BHiPass4.ar(csig,40);
			analysis_sig = Select.ar(isPredicting,[csig,sound_in]);
			mfccs = FluidMFCC.kr(analysis_sig,nMFCCs,startCoeff:1,maxNumCoeffs:nMFCCs);
			trig = Impulse.kr(30);
			mfccbuf_norm = LocalBuf(nMFCCs);
			parambuf_norm = LocalBuf(3);

			mfccs = FluidStats.kr(mfccs,ControlRate.ir * avg_win)[0];
			FluidKrToBuf.kr(mfccs,mfccbuf);

			scaler_mfcc.kr(trig * isPredicting,mfccbuf,mfccbuf_norm);
			nn.kr(trig * isPredicting,mfccbuf_norm,parambuf_norm);
			scaler_params.kr(trig * isPredicting,parambuf_norm,parambuf,invert:1);

			SendReply.kr(trig * isPredicting,"/params",params);
			SendReply.kr(trig,"/mfccs",mfccs);

			csig = csig.dup;
			csig * Select.kr(isPredicting,[vol.dbamp,Amplitude.kr(sound_in)]);
		}.play;

		s.sync;

		win.view.decorator_(FlowLayout(Rect(0,0,win.bounds.width,win.bounds.height)));


		param_sliders[0] = EZSlider(win,Rect(0,0,item_width,20),"carrier freq",\freq.asSpec,{arg sl; parambuf.set(0,sl.value)},440,true,label_width);

		win.view.decorator.nextLine;

		param_sliders[1] = EZSlider(win,Rect(0,0,item_width,20),"mod freq",\freq.asSpec,{arg sl; parambuf.set(1,sl.value)},100,true,label_width);

		win.view.decorator.nextLine;

		param_sliders[2] = EZSlider(win,Rect(0,0,item_width,20),"index",ControlSpec(0,20),{arg sl; parambuf.set(2,sl.value)},10,true,label_width);

		win.view.decorator.nextLine;

		EZSlider(win,Rect(0,0,item_width,20),"params avg smooth",nil.asSpec,{arg sl; synth.set(\avg_win,sl.value)},0,true,label_width);
		win.view.decorator.nextLine;

		StaticText(win,Rect(0,0,label_width,20)).string_("% MFCCs".format(nMFCCs));
		win.view.decorator.nextLine;

		statsWinSl = EZSlider(win,Rect(0,0,item_width,20),"fmcc avg smooth",nil.asSpec,{arg sl; synth.set(\avg_win,sl.value)},0,true,label_width);
		win.view.decorator.nextLine;

		mfcc_multslider = MultiSliderView(win,Rect(0,0,item_width,200))
		.size_(nMFCCs)
		.elasticMode_(true);

		win.view.decorator.nextLine;

		Button(win,Rect(0,0,100,20))
		.states_([["Add Point"]])
		.action_{
			add_point.value;
		};

		win.view.decorator.nextLine;

		// spacer
		StaticText(win,Rect(0,0,label_width,20));
		win.view.decorator.nextLine;

		// MLP Parameters
		StaticText(win,Rect(0,0,label_width,20)).align_(\right).string_("hidden layers");
		hidden_tf = TextField(win,Rect(0,0,item_width - label_width,20))
		.string_(nn.hidden.asString.replace(", "," ")[2..(nn.hidden.asString.size-3)])
		.action_{
			arg tf;
			var hidden_ = "[%]".format(tf.string.replace(" ",",")).interpret;
			nn.hidden_(hidden_);
			// nn.prGetParams.postln;
		};

		win.view.decorator.nextLine;
		StaticText(win,Rect(0,0,label_width,20)).align_(\right).string_("activation");
		act_pum = PopUpMenu(win,Rect(0,0,item_width - label_width,20))
		.items_(["identity","sigmoid","relu","tanh"])
		.value_(nn.activation)
		.action_{
			arg pum;
			nn.activation_(pum.value);
			// nn.prGetParams.postln;
		};

		win.view.decorator.nextLine;
		StaticText(win,Rect(0,0,label_width,20)).align_(\right).string_("output activation");
		outAct_pum = PopUpMenu(win,Rect(0,0,item_width - label_width,20))
		.items_(["identity","sigmoid","relu","tanh"])
		.value_(nn.outputActivation)
		.action_{
			arg pum;
			nn.outputActivation_(pum.value);

			// nn.prGetParams.postln;
		};

		win.view.decorator.nextLine;
		maxIter_nb = EZNumber(win,Rect(0,0,item_width,20),"max iter",ControlSpec(1,10000,step:1),{
			arg nb;
			nn.maxIter_(nb.value.asInteger);

			// nn.prGetParams.postln;
		},nn.maxIter,false,label_width);

		win.view.decorator.nextLine;
		learnRate_nb = EZNumber(win,Rect(0,0,item_width,20),"learn rate",ControlSpec(0.001,1.0),{
			arg nb;
			nn.learnRate_(nb.value);

			// nn.prGetParams.postln;
		},nn.learnRate,false,label_width);

		win.view.decorator.nextLine;
		momentum_nb = EZNumber(win,Rect(0,0,item_width,20),"momentum",ControlSpec(0,1),{
			arg nb;
			nn.momentum_(nb.value);

			// nn.prGetParams.postln;
		},nn.momentum,false,label_width);

		win.view.decorator.nextLine;
		batchSize_nb = EZNumber(win,Rect(0,0,item_width,20),"batch size",ControlSpec(1,1000,step:1),{
			arg nb;
			nn.batchSize_(nb.value.asInteger);

			// nn.prGetParams.postln;
		},nn.batchSize,false,label_width);

		win.view.decorator.nextLine;

		Button(win,Rect(0,0,100,20))
		.states_([["Train"]])
		.action_{
			train.value;
		};

		Button(win,Rect(0,0,200,20))
		.states_([["Continuous Training Off"],["Continuous Training On"]])
		.action_{
			arg but;
			continuous_training = but.value.asBoolean;
			train.value;
		};

		win.view.decorator.nextLine;

		loss_st = StaticText(win,Rect(0,0,item_width,20)).string_("loss:");

		win.view.decorator.nextLine;
		Button(win,Rect(0,0,100,20))
		.states_([["Not Predicting"],["Predicting"]])
		.action_{
			arg but;
			synth.set(\isPredicting,but.value);
		};

		win.view.decorator.nextLine;

		Button(win,Rect(0,0,100,20))
		.states_([["Save MLP"]])
		.action_{
			Dialog.savePanel({
				arg path;
				nn.write(path);
			});
		};

		Button(win,Rect(0,0,100,20))
		.states_([["Open MLP"]])
		.action_{
			Dialog.openPanel({
				arg path;
				open_mlp.(path);
			});
		};

		win.bounds_(win.view.decorator.used);
		win.front;

		OSCdef(\mfccs,{
			arg msg;
			// msg.postln;
			defer{
				mfcc_multslider.value_(msg[3..].linlin(-40,40,0,1));
			};
		},"/mfccs");

		OSCdef(\params,{
			arg msg;
			// msg.postln;
			defer{
				param_sliders.do{
					arg sl, i;
					sl.value_(msg[3 + i]);
				};
			};
		},"/params");

		s.sync;

/*		statsWinSl.valueAction_(0.1);

		100.do{
			var cfreq = exprand(20,20000);
			var mfreq = exprand(20,20000);
			var index = rrand(0.0,20);
			parambuf.setn(0,[cfreq,mfreq,index]);
			0.2.wait;
			add_point.value;
			0.05.wait;
		};*/

		/*		100.do{
		var cfreq = exprand(60,4000);
		var mfreq = exprand(60,1000).clip(0,cfreq);
		var index = rrand(0.0,20);
		parambuf.setn(0,[cfreq,mfreq,index]);
		0.2.wait;
		add_point.value;
		0.05.wait;
		};*/

	}.play(AppClock);
};
)