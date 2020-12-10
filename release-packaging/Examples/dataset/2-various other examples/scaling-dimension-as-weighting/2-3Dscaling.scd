// Make:
// - A kmeans
// - a datasetquery
// - a normalizer
// - a standardizer
// - 3 DataSets of example points R-G-B descriptions
// - 3 DataSets for the scaled versions
// - 1 summative dataset and a LabelSet for predicted labels

(
~classifier = FluidKMeans(s,5, 1000);
~query = FluidDataSetQuery(s);
~stan = FluidStandardize(s);
~norm = FluidNormalize(s);
~sourceR = FluidDataSet(s,\a3DsourceR);
~sourceG = FluidDataSet(s,\a3DsourceG);
~sourceB = FluidDataSet(s,\a3DsourceB);
~scaledR = FluidDataSet(s,\a3DscaledR);
~scaledG = FluidDataSet(s,\a3DscaledG);
~scaledB = FluidDataSet(s,\a3DscaledB);
~composited = FluidDataSet(s,\a3Dcomposited);
~labels = FluidLabelSet(s,\a3Dlabels);
)

//Make some random, but clustered test points, each descriptor category in a separate dataset
(
~sourceR.load(Dictionary.newFrom([\cols, 1, \data, (Dictionary.newFrom(40.collect{|x| [x, 1.0.sum3rand]}.flatten))]));
~sourceG.load(Dictionary.newFrom([\cols, 1, \data, (Dictionary.newFrom(40.collect{|x| [x, 1.0.rand2]}.flatten))]));
~sourceB.load(Dictionary.newFrom([\cols, 1, \data, (Dictionary.newFrom(40.collect{|x| [x, (0.5.sum3rand).squared + [0.75,-0.1].choose]}.flatten))]));
)

//here we manipulate

//assemble the scaled dataset
(
~query.addColumn(0, {
	~query.transformJoin(~sourceB, ~sourceG, ~composited, {
		~query.transformJoin(~sourceR, ~composited, ~composited);
	});
});
)

~composited.print

//Fit the classifier to the example DataSet and labels, and then run prediction on the test data into our mapping label set
~classifier.fitPredict(~composited,~labels,{~labels.dump{|x|~labeldict = x;};~composited.dump{|x|~compodict=x;};});

//Visualise:
(
w = Window("sourceClasses", Rect(128, 64, 820, 120));
w.drawFunc = {
	Pen.use{
		~compodict["data"].keysValuesDo{|key,  colour|
			Pen.fillColor = Color.fromArray((colour * 0.5 + 0.5 ).clip(0,1) ++ 1);
			Pen.fillRect( Rect( (key.asFloat * 20 + 10), (~labeldict["data"].at(key).asInteger[0] * 20 + 10),15,15));
		};
	};
};
w.refresh;
w.front;
)

// standardize our colours and rerun
(
~stan.fitTransform(~sourceR, ~scaledR, {
	~stan.fitTransform(~sourceG, ~scaledG, {
		~stan.fitTransform(~sourceB, ~scaledB, {
			//assemble
			~query.addColumn(0, {
				~query.transformJoin(~scaledB, ~scaledG, ~composited, {
					~query.transformJoin(~scaledR, ~composited, ~composited, {
						//fit
						~classifier.fitPredict(~composited,~labels,{~labels.dump{|x|~labeldict2 = x;};~composited.dump{|x|~compodict2=x;};});
					});
				});
			});
		});
	});
});
)

//Visualise:
(
w = Window("stanClasses", Rect(128, 64, 820, 120));
w.drawFunc = {
	Pen.use{
		~compodict2["data"].keysValuesDo{|key,  colour|
			Pen.fillColor = Color.fromArray((colour * 0.25 + 0.5 ).clip(0,1) ++ 1);
			Pen.fillRect( Rect( (key.asFloat * 20 + 10), (~labeldict2["data"].at(key).asInteger[0] * 20 + 10),15,15));
		};
	};
};
w.refresh;
w.front;
)

//now let's normalise instead
(
~norm.fitTransform(~sourceR, ~scaledR, {
	~norm.fitTransform(~sourceG, ~scaledG, {
		~norm.fitTransform(~sourceB, ~scaledB, {
			//assemble
			~query.addColumn(0, {
				~query.transformJoin(~scaledB, ~scaledG, ~composited, {
					~query.transformJoin(~scaledR, ~composited, ~composited, {
						//fit
						~classifier.fitPredict(~composited,~labels,{~labels.dump{|x|~labeldict2 = x;};~composited.dump{|x|~compodict2=x;};});
					});
				});
			});
		});
	});
});
)

//Visualise:
(
w = Window("normClasses", Rect(128, 64, 820, 120));
w.drawFunc = {
	Pen.use{
		~compodict2["data"].keysValuesDo{|key,  colour|
			Pen.fillColor = Color.fromArray((colour * 0.25 + 0.5 ).clip(0,1) ++ 1);
			Pen.fillRect( Rect( (key.asFloat * 20 + 10), (~labeldict2["data"].at(key).asInteger[0] * 20 + 10),15,15));
		};
	};
};
w.refresh;
w.front;
)

// let's mess up with the scaling of one dimension: let's multiply the range of Red by 10
~norm.min = -10;
~norm.max = 10;
(
~norm.fitTransform(~sourceR, ~scaledR, {
	//assemble
	~query.addColumn(0, {
		~query.transformJoin(~scaledB, ~scaledG, ~composited, {
			~query.transformJoin(~scaledR, ~composited, ~composited, {
				//fit
				~classifier.fitPredict(~composited,~labels,{~labels.dump{|x|~labeldict2 = x;};~composited.dump{|x|~compodict2=x;};});
			});
		});
	});
});
)

//Visualise:
(
w = Window("norm10rClasses", Rect(128, 64, 820, 120));
w.drawFunc = {
	Pen.use{
		~compodict2["data"].keysValuesDo{|key,  colour|
			Pen.fillColor = Color.fromArray((colour * 0.25 + 0.5 ).clip(0,1) ++ 1);
			Pen.fillRect( Rect( (key.asFloat * 20 + 10), (~labeldict2["data"].at(key).asInteger[0] * 20 + 10),15,15));
		};
	};
};
w.refresh;
w.front;
)
