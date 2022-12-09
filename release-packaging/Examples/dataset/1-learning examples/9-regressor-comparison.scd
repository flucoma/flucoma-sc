//In this patch we will try to make sense of a few behaviours of the regressors we have. This is a toy example with 3 then 4 samples, but it showcases a few of the behaviours and assumptions we might have that might be innaccurate.

//Here we make a 3-points pair of dataset.

~dsIN = FluidDataSet(s);
~dsIN.load(Dictionary.newFrom([\cols, 1, \data, Dictionary.newFrom([\point1, [10], \point2, [20], \point3, [30]])]));
~dsOUT = FluidDataSet(s);
~dsOUT.load(Dictionary.newFrom([\cols, 1, \data, Dictionary.newFrom([\point1, [0.8], \point2, [0.2], \point3, [0.5]])]));

//check the values
~dsIN.print;~dsOUT.print;
// make a graph to visualise it
(0.dup(10) ++ 0.8 ++ 0.dup(9) ++ 0.2 ++ 0.dup(9) ++ 0.5 ++ 0.dup(10)).plot(\source,discrete: true, minval:0, maxval: 1).plotMode=\bars;

//Let's make a complete dataset to predict each points in our examples:
~dsALLin = FluidDataSet(s);
~dsALLin.load(Dictionary.newFrom([\cols, 1, \data, Dictionary.newFrom(Array.fill(41,{|x| [x.asSymbol, [x]];}).flatten(1);)]));
~dsALLin.print

//We'll regress these values via KNN and plot
~regK = FluidKNNRegressor(s,numNeighbours: 2,weight: 1);
~dsALLknn = FluidDataSet(s);
~regK.fit(~dsIN,~dsOUT);
~regK.predict(~dsALLin,~dsALLknn);

//retrieve and plot our data
(
~knnALLval = Array.new(41);
~dsALLknn.dump{|x| 41.do{|i|
	~knnALLval.add((x["data"][i.asString]))
}};
)
~knnALLval.flatten(1).plot(\knn,discrete: true, minval:0, maxval: 1).plotMode=\bars;

//Regressing directly these value-pairs in knn we see a full set of values being predicted: we can see what looks like linear interpolation, but not outside the boundaries. This is because we make a weighted average of the nearest 2 neigbourgs, which are not necessarily around the predicted value, they might both be on the same side like 0 to 9 (10 and 20 are nearest) and 31 to 40 (20 and 30 are nearest).

//Let's do the same process with MLP
~regM = FluidMLPRegressor(s,hidden: [4],activation: 1,outputActivation: 1,maxIter: 10000,learnRate: 0.1,momentum: 0,batchSize: 1,validation: 0);
~dsALLmlp = FluidDataSet(s);
~regM.fit(~dsIN,~dsOUT,{|x|x.postln;});
~regM.predict(~dsALLin,~dsALLmlp);

(
~mlpALLval = Array.new(41);
~dsALLmlp.dump{|x| 41.do{|i|
	~mlpALLval.add((x["data"][i.asString]))
}};
)
~mlpALLval.flatten(1).plot(\mlp_full_range,discrete: true, minval:0, maxval: 1).plotMode=\bars;

//We see that we have a large bump and nothing else. This is because our input are very large (10-30) and outside the optimal range of the activation function (0-1 for sigmoid) so our network saturates and cannot recover. If we normalise our inputs and we rerun the network we get a curve that fits the 3 values. You can fit more than once to get more iterations and lower the error.

~norm = FluidNormalize(s);
~norm.fitTransform(~dsIN,~dsIN); //fit and normalise in place our training data
~norm.transform(~dsALLin, ~dsALLin); //and normalise in place our sampling data
~regM.clear;//resets the seeds
~regM.fit(~dsIN,~dsOUT,{|x|x.postln;}); //a few hits will show a very low error
~regM.predict(~dsALLin,~dsALLmlp);

(
~mlpALLval = Array.new(41);
~dsALLmlp.dump{|x| 41.do{|i|
	~mlpALLval.add((x["data"][i.asString]))
}};
)
~mlpALLval.flatten(1).plot(\mlp_normalized,discrete: true, minval:0, maxval: 1).plotMode=\bars;

//Now we can add one value to our sparse dataset. Note that we revert back to full range values here for the example
~dsIN.load(Dictionary.newFrom([\cols, 1, \data, Dictionary.newFrom([\point1, [10], \point2, [20], \point3, [30], \point4, [22]])]));
~dsOUT.load(Dictionary.newFrom([\cols, 1, \data, Dictionary.newFrom([\point1, [0.8], \point2, [0.2], \point3, [0.5], \point4, [0.26]])]));
~source = (0.dup(10) ++ 0.8 ++ 0.dup(9) ++ [0.2, 0, 0.26] ++ 0.dup(7) ++ 0.5 ++ 0.dup(10)); ~source.plot(\source,discrete: true, minval:0, maxval: 1).plotMode=\bars;

//Before re-processing, trying to estimate what will happen is a good exercice. Then execute the code below.

(
~norm.fitTransform(~dsIN,~dsIN, { //fit and normalise in place our training data (our sampling data is still normalised to the same range
	~regK.fit(~dsIN,~dsOUT, { //knn fit
		~regK.predict(~dsALLin,~dsALLknn, { //knn predict
			~regM.fit(~dsIN,~dsOUT,{|x|x.postln; //mlp fit
				~regM.predict(~dsALLin,~dsALLmlp, { //mlp predict
					~mlpALLval = Array.new(41); //mlp retrieve
					~dsALLmlp.dump{|x| 41.do{|i|
						~mlpALLval.add((x["data"][i.asString]));
					}};
					~knnALLval = Array.new(41);//knn retrieve
					~dsALLknn.dump{|x| 41.do{|i|
						~knnALLval.add((x["data"][i.asString]));
					};//draw everything
					{[~source, ~knnALLval.flatten(1), ~mlpALLval.flatten(1)].flop.flatten(1).plot(\source,numChannels: 3, discrete: false, minval:0, maxval: 1).plotMode=\bars;}.defer;
					};
				});
			});
		});
	});
});
)

//The first thing to meditate upon is how much of a kink assymetrically sampled data makes in nearest-distance regression. That can be soften by having a larger number of neigbourgs but not much. With this in mind, example 4-regressor-example.maxpat should be explored again. This is where neural net shines. Its curve barely moved and still matches the data. Interestingly, it provides values that are somehow sensible outside of the training dataset too.