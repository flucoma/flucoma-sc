(
~simpleInput = FluidDataSet(s,\simpleInput,2);
~simpleOutput = FluidLabelSet(s,\simpleOutput,2);
b = Buffer.alloc(s,2);
~knn = FluidKNN(s);
k = 3
)

(
var w,v,myx,myy;

//initialise the mouse position holder
myx=0;
myy=0;

//make a window and a full size view
w = Window.new("Viewer",  Rect(100,Window.screenBounds.height - 400, 310, 310)).front;
v = View.new(w,Rect(0,0, 310, 310));

//creates a function that reacts to mousedown
v.mouseDownAction = {|view, x, y|myx=x;myy=y;w.refresh;
	// myx.postln;myy.postln;
	Routine{
		b.setn(0,[myx,myy]);
		s.sync;
		~knn.classifyPoint(b, ~simpleOutput, k, {|x|x.postln;});
}.play;};

//custom redraw function
w.drawFunc = {
	100.do { |i|
		if (i < 50, {Pen.color = Color.white;} ,{Pen.color = Color.red;});
		Pen.addRect(Rect(i.div(10)*30+10,i.mod(10)*30+10,20,20));
		Pen.perform(\fill);
	};
	Pen.color = Color.black;
	Pen.addOval(Rect(myx-5, myy-5,10,10));
	Pen.perform(\stroke);
};
)

(
//populates a dataset with the same squares as the gui (their centres)
Routine{
	50.do{|i|
	var	x = i.div(10)*30+20;
			var y = i.mod(10)*30+20;
	b.setn(0,[x,y]);
		s.sync;
		~simpleInput.addPoint(i.asString,b,{("Added Input" + i).postln});
			~simpleOutput.addLabel(i.asString,"White",{("Added Output" + i).postln});
			b.setn(0,[x+150,y]);
		s.sync;
		~simpleInput.addPoint((i+50).asString,b,{("Added Input" + (i+50)).postln});
		~simpleOutput.addLabel((i+50).asString,"Red",{("Added Output" + (i+50)).postln});
	}
	}.play;
)

// fit the dataset
~knn.fit(~simpleInput,action:{"fitting done".postln})

// now click on the grid and read the estimated class according to the nearest K neighbours.
