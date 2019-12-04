(
var w,v,myx,myy;
myx=0;
myy=0;
w = Window.new("Viewer",  Rect(100,Window.screenBounds.height - 400, 310, 310)).front;
v = View.new(w,Rect(0,0, 310, 310));
// v.mouseMoveAction = {|view, x, y, modifiers|"% % % %\n".postf(view, x, y, modifiers);};
v.mouseMoveAction = {|view, x, y, modifiers|myx=x;myy=y;myx.postln;myy.postln;w.refresh;};

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
