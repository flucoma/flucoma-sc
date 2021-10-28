(
~dummy_data = {
	Dictionary.newFrom([
		"cols",2,
		"data",Dictionary.newFrom(Array.fill(200,{
			arg i;
			var return;
			if((i % 2) == 0,{
				return = "example-%".format((i/2).asInteger);
			},{
				return = [exprand(20,20000),rrand(-130,0)];
			});
			// return.postln;
			return;
		}))
	]);
};
)

(
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

~fp.pointSize_(6)

(
10.do({
	~fp.setColor_("example-%".format(rrand(0,99)),Color.yellow);
});
)

~fp.highlight_("example-90",Color.red);

~fp.dict_(~dummy_data.value);

~fp.ymin_(-50)

~fp.shape_(\square)


// with parent
(
Window.closeAll;
d = Dictionary.newFrom([
	"cols",2,
	"data",Dictionary.newFrom(Array.fill(200,{
		arg i;
		var return;
		if((i%2) == 0,{
			return = "exmaple-%".format((i/2).asInteger);
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

~fp.setColor_("example-7",Color.red);