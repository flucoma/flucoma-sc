FluidPlotter {
	var <parent, <userView, <xmin, <xmax, <ymin, <ymax, <pointSize = 4, colors, dict, shape = \circle, catColors;

	*new {
		arg parent, bounds, dict, mouseMoveAction,xmin = 0,xmax = 1,ymin = 0,ymax = 1;
		^super.new.init(parent, bounds, dict, mouseMoveAction,xmin,xmax,ymin,ymax);
	}

	init {
		arg parent_, bounds, dict_, mouseMoveAction, xmin_ = 0,xmax_ = 1,ymin_ = 0,ymax_ = 1;

		parent = parent_;
		xmin = xmin_;
		xmax = xmax_;
		ymin = ymin_;
		ymax = ymax_;

		if(dict_.isNil,{this.dictNotProperlyFormatted});
		if(dict_.size != 2,{this.dictNotProperlyFormatted});
		if(dict_.at("data").isNil,{this.dictNotProperlyFormatted});
		if(dict_.at("cols").isNil,{this.dictNotProperlyFormatted});
		if(dict_.at("cols") != 2,{this.dictNotProperlyFormatted});

		this.createCatColors;

		this.createPlotWindow(bounds,parent_,mouseMoveAction,dict_);
	}

	createCatColors {
		catColors = "1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf".clump(6).do({
			arg hex;
			Color.newHex(hex);
		});
	}

	setCategories_ {
		arg labelSetDict;
		labelSetDict.postln;
	}

	setColor_ {
		arg identifier, color;
		if(dict.at("data").at(identifier).notNil,{
			colors.put(identifier,color);
			this.refresh;
		},{
			"FluidPlotter::setColor_ identifier not found".warn;
		});
	}

	shape_ {
		arg sh;
		shape = sh;
		this.refresh;
	}

	background_ {
		arg bg;

	}

	refresh {
		userView.refresh;
	}

	pointSize_ {
		arg ps;
		pointSize = ps;
		this.refresh;
	}

	dict_ {
		arg d;
		dict = d;
		colors = Dictionary.newFrom(dict.at("data").keys.collect({
			arg key;
			[key,Color.black];
		}).asArray.flatten);
		this.refresh;
	}

	xmin_ {
		arg val;
		xmin = val;
		this.refresh;
	}

	xmax_ {
		arg val;
		xmax = val;
		this.refresh;
	}

	ymin_ {
		arg val;
		ymin = val;
		this.refresh;
	}

	ymax_ {
		arg val;
		ymax = val;
		this.refresh;
	}

	highlight_ {
		arg identifier, color;
		var found = false;
		colors.keys.do{
			arg key;
			if(key == identifier,{
				colors.put(key,color);
				found = true;
			},{
				colors.put(key,Color.black);
			});
		};
		if(found,{
			this.refresh;
		},{
			"FluidPlotter::highlight_ identifier not found".warn;
		});
	}

	dictNotProperlyFormatted {
		"FluidPlotter: The dictionary passed in is not properly formatted.".error;
	}

	createPlotWindow {
		arg bounds,parent_, mouseMoveAction,dict_;
		var xpos, ypos;

		if(parent_.isNil,{xpos = 0; ypos = 0},{xpos = bounds.left; ypos = bounds.top});

		parent = parent_ ? Window("FluidPlotter",bounds);
		userView = UserView(parent,Rect(xpos,ypos,bounds.width,bounds.height));

		userView.drawFunc_({
			dict.at("data").keysValuesDo({
				arg key, pt;
				var scaledx = pt[0].linlin(xmin,xmax,0,userView.bounds.width) - (pointSize/2);
				var scaledy = pt[1].linlin(ymin,ymax,0,userView.bounds.height) - (pointSize/2);
				var color = colors.at(key);

				shape.switch(
					\square,{Pen.addRect(Rect(scaledx,scaledy,pointSize,pointSize))},
					\circle,{Pen.addOval(Rect(scaledx,scaledy,pointSize,pointSize))}
				);

				Pen.color_(color);
				Pen.draw;
			});
		});

		userView.mouseMoveAction_({
			arg view, x, y, modifiers;
			x = x.linlin(pointSize/2,userView.bounds.width-(pointSize/2),xmin,xmax);
			y = y.linlin(pointSize/2,userView.bounds.height-(pointSize/2),ymin,ymax);
			mouseMoveAction.(this,x,y,modifiers);
		});

		if(parent_.isNil,{parent.front;});

		this.dict_(dict_);
	}

	close {
		parent.close;
	}
}