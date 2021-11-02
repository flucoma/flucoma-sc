(
// make some dummy data and plot it
~dummy_data = {
	arg xmin = 20, xmax = 20000, ymin = -130, ymax = 0;
	Dictionary.newFrom([
		"cols",2,
		"data",Dictionary.newFrom(Array.fill(200,{
			arg i;
			var return;
			if((i % 2) == 0,{
				return = "example-%".format((i/2).asInteger);
			},{
				return = [rrand(xmin,xmax),rrand(ymin,ymax)];
			});
			// return.postln;
			return;
		}))
	]);
};

Window.closeAll;
// self window
d = ~dummy_data.value;
// d.postln;
~fp = FluidPlotter(bounds:Rect(200,200,600,600),dict:d,mouseMoveAction:{
	arg view, x, y, modifiers;
	[view, x, y, modifiers].dopostln;
	"".postln;
},xmin:20,xmax:20000,ymin:-130,ymax:0);
)

// click and drag on the plotter to report stuff in the mouseMoveAction callback function

// change point size of just one point
~fp.pointSize_("example-5",10);

// change it back
~fp.pointSize_("example-5",1);

// change all points size bigger...
~fp.pointSizeScale_(2);

// ...smaller...
~fp.pointSizeScale_(0.5);

// ...back to normal
~fp.pointSizeScale_(1);

(
// change 10 random points red
10.do({
	~fp.pointColor_("example-%".format(rrand(0,99)),Color.red);
});
)
// "highlight" a point (makes it a little bigger)
~fp.highlight_("example-95");

// a different one
~fp.highlight_("example-94");

// none
~fp.highlight_(nil);

// put some different data in
~fp.dict_(~dummy_data.value);

// change the ranges
(
~fp.ymin_(-140);
~fp.ymax_(10);
~fp.xmin_(-200);
~fp.xmax_(21000);
)

// change the point shapes
~fp.shape_(\square);

// change back to circles
~fp.shape_(\circle);

// change the color of just one point
~fp.pointColor_("example-7",Color.red);

// change the background color
~fp.background_(Color.red)
~fp.background_(Color.white)

// ==== perform KMeans on the data and colorize the categories ======
(
s.waitForBoot{
	Routine{
		var labelset = FluidLabelSet(s);
		var kmeans = FluidKMeans(s);
		var ds = FluidDataSet(s);

		s.sync;

		ds.load(~fp.dict,{
			kmeans.fitPredict(ds,labelset,{
				labelset.dump({
					arg lsdict;
					defer{~fp.categories_(lsdict)};
					"done".postln;
				});
			});
		});
	}.play;
}
)

// close it or it's parent
~fp.close;


// a FluidPlotter inside a parent with parent
(
Window.closeAll;
d = Dictionary.newFrom([
	"cols",2,
	"data",Dictionary.newFrom(Array.fill(200,{
		arg i;
		var return;
		if((i%2) == 0,{
			return = "example-%".format((i/2).asInteger);
		},{
			return = [exprand(20,20000),rrand(-130,0)];
		});
		return;
	}))
]);
w = Window("test",Rect(50,50,800,600)).front;
~fp = FluidPlotter(w,Rect(50,50,400,400),dict:d,mouseMoveAction:{
	arg view, x, y, modifiers;
	[view, x, y, modifiers].dopostln;
	"".postln;
},xmin:20,xmax:20000,ymin:-130,ymax:0);
)

// you can make an empty one and then set the dict later
(
Window.closeAll;
~fp = FluidPlotter(bounds:Rect(100,100,500,500))
)

// now set data
~fp.dict_(~dummy_data.(0.01,1,0.0,1.0).postln);