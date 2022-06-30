s.reboot;
//Preliminaries: we want some audio, a couple of FluidDataSets, some Buffers
(
~raw = FluidDataSet(s);
~norm = FluidDataSet(s);
~retrieved = FluidDataSet(s);
~audio = Buffer.read(s,FluidFilesPath("Tremblay-ASWINE-ScratchySynth-M.wav"));
~melfeatures = Buffer.new(s);
~stats = Buffer.alloc(s, 7, 40);
~datapoint = Buffer.alloc(s, 40);
~queryPoint = Buffer.alloc(s, 2);
~dQueryPoint = Buffer.alloc(s, 2);
~dpN =Buffer.alloc(s, 40);
~dpMLPn =Buffer.alloc(s, 40);
~dpMLP =Buffer.alloc(s, 40);
)

// process the melbands
FluidBufMelBands.process(s,~audio, features: ~melfeatures,action: {\done.postln;});

// Divide the time series in 100, and take the mean of each segment and add this as a point to the 'raw' FluidDataSet
(
{
	var trig = LocalIn.kr(1, 1);
	var buf =  LocalBuf(40, 1);
	var count = PulseCount.kr(trig) - 1;
	var chunkLen = (~melfeatures.numFrames / 100).asInteger;
	var stats = FluidBufStats.kr(source: ~melfeatures, startFrame: count * chunkLen, numFrames: chunkLen, stats: ~stats, trig: trig, blocking: 1);
	var rd = BufRd.kr(40, ~stats, DC.kr(0), 0, 1);
	var bufWr, dsWr;
	40.do{|i|
		bufWr = BufWr.kr(rd[i], buf, DC.kr(i));
	};
	dsWr = FluidDataSetWr.kr(~raw, buf: buf, idNumber: count, trig: Done.kr(stats));
	LocalOut.kr( Done.kr(dsWr));
	FreeSelf.kr(count - 99);
	Poll.kr(trig,(100-count));
}.play;
)
// wait for the count to reaches 0 in the post window. Check the dataset if curious (loads of small numbers)
~raw.print;

// normalize the input
~normalizer = FluidNormalize(s);
~normalizer.fitTransform(~raw,~norm);
~norm.print; //a more decent range

//we can then run the AE - the server might become yellow :)
~mlp = FluidMLPRegressor(s,[9,2,9],activation: 1,outputActivation: 1,tapIn: 0,tapOut: 2,maxIter: 10000,learnRate: 0.1,momentum: 0.1,batchSize: 10,validation: 0.1);
~mlp.fit(~norm,~norm,{|x|x.postln;});// run this a few times, until you are happy with the error

//we can then retrieve the hidden layer #2 because of the tapOut parameter
~mlp.predict(~norm,~retrieved);

//check the structure of retrieved
~retrieved.print;

//let's normalise it for display
~normData = FluidDataSet(s);
~reducedarray = Array.new(100);
~normalView = FluidNormalize(s,0.1,0.9);
(
~normalView.fitTransform(~retrieved,~normData, action:{
	~normData.dump{|x| 100.do{|i|
		~reducedarray.add(x["data"][i.asString])
	}};
});
)
~normData.print;

//make a basic KD tree to retrieve the nearest entry in 2D
~kdtree = FluidKDTree(s,numNeighbours: 1);
~kdtree.fit(~normData);

//prepare the normalizers and the neural net for inverse query
(
~mlp.tapIn = 2;
~mlp.tapOut = -1;
)

//Visualise and query the 2D projection of our original 40D data
(
var w,v,myx,myy,vRNN, vRN, vMN, vM;

~arrayRawNn = Array.new(40);
~arrayRawN = Array.new(40);
~arrayMLPn = Array.new(40);
~arrayMLP = Array.new(40);

//initialise the mouse position holder
myx=130;
myy=130;

w = Window("AutoEncoder", Rect(64, 64, 770, 270));
v = View.new(w,Rect(0,0, 310, 310));

vRNN = MultiSliderView(w,Rect(270,8,240,115)).value_(~arrayRawNn).readOnly_(true).elasticMode_(1).isFilled_(true);
vRN = MultiSliderView(w,Rect(270,147,240,115)).value_(~arrayRawN).readOnly_(true).elasticMode_(1).isFilled_(true);
vMN = MultiSliderView(w,Rect(520,10,240,115)).value_(~arrayMLPn).readOnly_(true).elasticMode_(1).isFilled_(true);
vM = MultiSliderView(w,Rect(520,147,240,115)).value_(~arrayMLP).readOnly_(true).elasticMode_(1).isFilled_(true);

StaticText(w,Rect(275,120,490,30)).string_("above: normalised nearest neighbour\nbelow: original nearest neighbour").font_(Font("Monaco", 10));
StaticText(w,Rect(525,120,490,30)).string_("above: regressed values at coordinates\nbelow: denormalised regressed values").font_(Font("Monaco", 10));

//creates a function that reacts to mousedown
v.mouseMoveAction = {|view, x, y|
	myx=x.clip(10,260);
	myy=y.clip(10,260);
	w.refresh;
	Routine{
		~queryPoint.setn(0,([myx,myy] - 10 / 250));//set the query point to the coordinate
		~kdtree.kNearest(~queryPoint, action: {|nearest| //retrieve the nearest point
			~norm.getPoint(nearest, ~dpN, action: { //get the normalised 40d
				~raw.getPoint(nearest, ~datapoint, action: { // get the original 40d
					~normalView.inverseTransformPoint(~queryPoint, ~dQueryPoint, action: { //denormalise the 2d coordinate to get the right range of values for the MLP
						~mlp.predictPoint(~dQueryPoint, ~dpMLPn, action:  { //predict from the middle (2d) to the normalised output (40d)
							~normalizer.inverseTransformPoint(~dpMLPn, ~dpMLP, action:  { //denormalised the 40d
								~datapoint.getn(0,40,{|x|~arrayRawN = x; //retrieve the nearest
									~dpN.getn(0,40,{|x|~arrayRawNn = x; // retrieve the normalised nearest
										~dpMLPn.getn(0,40,{|x|~arrayMLPn = x; //retrieve the predicted normalised 40d
											~dpMLP.getn(0,40,{|x|~arrayMLP = x; //retrieve the denormalised predicted 40d
												AppClock.sched(0,{ // update the visualisation of the 4 arrays
													vRNN.value=~arrayRawNn;
													vRN.value=~arrayRawN * 15;
													vMN.value=~arrayMLPn;
													vM.value=~arrayMLP * 15;
												});
											});
										});
									});
								});
							});
						});
					});
				});
			});
		});
	}.play;
};
//custom redraw function
w.drawFunc = {
	Pen.use {
		~reducedarray.size.do{|i|
			var coord = (~reducedarray[i] * 250) + 7;
			var r = Rect(coord[0],coord[1],6,6);
			Pen.fillColor = Color.blue;
			Pen.fillOval(r);
		};
	};
	Pen.color = Color.red;
	Pen.addOval(Rect(myx-4, myy-4,8,8));
	Pen.perform(\stroke);
	Pen.color = Color.black;
	Pen.addRect(Rect(10,10,250,250));
	Pen.perform(\stroke);
};
w.refresh;
w.front;
)