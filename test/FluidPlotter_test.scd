(
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

~fp.pointSize_(10);

(
10.do({
	~fp.setColor_("example-%".format(rrand(0,99)),Color.yellow);
});
)

~fp.highlight_("example-95");
~fp.highlight_(nil);

~fp.dict_(~dummy_data.value);

(
~fp.ymin_(-140);
~fp.ymax_(10);
~fp.xmin_(-200);
~fp.xmax_(21000);
)

~fp.shape_(\square);

~fp.setSizeMultiplier_("example-0",10);
~fp.setSizeMultiplier_("example-0",1);

~fp.setColor_("example-7",Color.red);

~fp.background_(Color.white)
~fp.background_(Color.red)

~fp.close;

(
s.waitForBoot{
	Routine{
		var labelset = FluidLabelSet(s);
		var kmeans = FluidKMeans(s);
		var ds = FluidDataSet(s);

		s.sync;

		ds.load(~dummy_data.(),{
			kmeans.fitPredict(ds,labelset,{
				labelset.dump({
					arg lsdict;
					defer{~fp.setCategories_(lsdict)};
					"done".postln;
				});
			});
		});
	}.play;
}
)


// with parent
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

(
Window.closeAll;
~fp = FluidPlotter(bounds:Rect(100,100,500,500))
)

~fp.dict_(~dummy_data.(0.01,1,0.0,1.0).postln);