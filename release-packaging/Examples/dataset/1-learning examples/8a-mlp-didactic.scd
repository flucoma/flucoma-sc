// basic xor to show non-linearity guestimating of MLP
// see https://medium.com/@jayeshbahire/the-xor-problem-in-neural-networks-50006411840b
// and https://youtu.be/188B6k_F9jU

// make 2 datasets as our truth
(
Routine{
	d = Dictionary.new;
	d.add(\cols -> 2);
	d.add(\data -> Dictionary.newFrom(["f-f", [0,0], "f-t", [0,1], "t-f", [1,0], "t-t", [1,1]]));
    ~mlpHelpSource = FluidDataSet.new(s);
	s.sync;
    ~mlpHelpSource.load(d);
	s.sync;
	d.add(\cols -> 1);
	d.add(\data -> Dictionary.newFrom(["f-f", [0], "f-t", [1], "t-f", [1], "t-t", [0]]));
    ~mlpHelpTarget = FluidDataSet.new(s);
	s.sync;
    ~mlpHelpTarget.load(d);
	s.sync;
}.play;
)

//check our thruth tables
~mlpHelpSource.print
~mlpHelpTarget.print

// make an MLPregressor
~mlp = FluidMLPRegressor(s, [3], FluidMLPRegressor.sigmoid, FluidMLPRegressor.sigmoid,maxIter:1000,learnRate: 0.1,momentum: 0.1,batchSize: 1,validation: 0);//1000 epoch at a time
//train it by executing the following line multiple time, and observe the error
~mlp.fit(~mlpHelpSource,~mlpHelpTarget,{|x|x.postln;});

//to make a plot of the error let's do a classic 'shades of truth' (a grid of 11 x 11 with each values of truth between 0 and 1
(
Routine{
		d = Dictionary.new;

	d.add(\cols -> 2);
	d.add(\data -> Dictionary.newFrom(121.collect{|x|[x.asString, [x.div(10)/10,x.mod(10)/10]]}.flatten));
    ~mlpHelpShades = FluidDataSet.new(s);
	s.sync;
    ~mlpHelpShades.load(d);
	s.sync;
}.play;
)

// check it
~mlpHelpShades.print

// let's make a destination for our regressions
~mlpHelpRegressed = FluidDataSet.new(s);

// then predict the full DataSet in our trained network
~mlp.predict(~mlpHelpShades,~mlpHelpRegressed);

// estimated values
~mlpHelpRegressed.print;

// lets extract them as an array
~assignments = Array.new(121); ~mlpHelpRegressed.dump{|x|~assignments = x.at("data").atAll(x.at("data").keys.asArray.sort{|a,b|a.asInteger < b.asInteger}).flatten.postln;};

//and draw them
(
w = Window("scatter", Rect(128, 64, 200, 200));
w.drawFunc = {
    Pen.use {
        ~assignments.do{|val,ind|
			var x = (ind.div(10)*20);
			var y = (ind.mod(10)*20);
            var r = Rect(x,y,20,20);
			Pen.fillColor = Color.grey(val);
            Pen.fillRect(r);
        }
    }
};
w.refresh;
w.front;
)

~mlp.free
~mlpHelpShades.free
~mlpHelpSource.free
~mlpHelpTarget.free