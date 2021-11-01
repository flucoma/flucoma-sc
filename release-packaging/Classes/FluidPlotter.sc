FluidPlotterPoint {
	var id, <x, <y, <>color, <>sizeMultiplier = 1;

	*new {
		arg id, x, y, color, sizeMultiplier = 1;
		^super.new.init(id,x,y,color,sizeMultiplier);
	}

	init {
		arg id_, x_, y_, color_, sizeMultiplier_ = 1;
		id = id_;
		x = x_;
		y = y_;
		color = color_ ? Color.black;
		sizeMultiplier = sizeMultiplier_;
	}
}

FluidPlotter {
	var <parent, <userView, <xmin, <xmax, <ymin, <ymax, <pointSize = 6, dict_internal, <dict, shape = \circle, catColors, highlightIdentifier;

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

		this.createCatColors;
		if(dict_.notNil,{this.dict_(dict_)});
		this.createPlotWindow(bounds,parent_,mouseMoveAction,dict_);
	}

	createCatColors {
		catColors = "1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf".clump(6).collect({
			arg hex;
			Color.newHex(hex);
		});
	}

	setCategories_ {
		arg labelSetDict;
		if(dict_internal.notNil,{
			dict_internal.keysValuesDo({
				arg id, pt;
				var col, cat = labelSetDict.at("data").at(id)[0].interpret;
				if(cat > (catColors.size-1),{"FluidPlotter:setCategories_ FluidPlotter doesn't have that many category colors. You can use the method 'setColor_' to set colors for individual points.".warn});
				col = catColors[cat];
				pt.color_(col);
			});
			this.refresh;
		},{
			"FluidPlotter::setCategories_ FluidPlotter cannot receive setCategories. It has no data. First set a dictionary.".warn;
		});
	}

	setSizeMultiplier_ {
		arg identifier, sizeMultiplier;
		if(dict_internal.at(identifier).notNil,{
			dict_internal.at(identifier).sizeMultiplier_(sizeMultiplier);
			this.refresh;
		},{
			"FluidPlotter::setSizeMultiplier_ identifier not found".warn;
		});
	}

	setPoint_ {
		arg identifier, x, y, sizeMultiplier = 1, color;
		dict_internal.put(identifier,FluidPlotterPoint(identifier,x,y,color ? Color.black,sizeMultiplier));
		this.refresh;
	}

	setColor_ {
		arg identifier, color;
		if(dict_internal.at(identifier).notNil,{
			dict_internal.at(identifier).color_(color);
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
		userView.background_(bg);
	}

	refresh {
		{userView.refresh}.defer;
	}

	pointSize_ {
		arg ps;
		pointSize = ps;
		this.refresh;
	}

	dict_ {
		arg d;

		if(d.isNil,{^this.dictNotProperlyFormatted});
		if(d.size != 2,{^this.dictNotProperlyFormatted});
		if(d.at("data").isNil,{^this.dictNotProperlyFormatted});
		if(d.at("cols").isNil,{^this.dictNotProperlyFormatted});
		if(d.at("cols") != 2,{^this.dictNotProperlyFormatted});

		dict = d;
		dict_internal = Dictionary.new;
		dict.at("data").keysValuesDo({
			arg k, v;
			dict_internal.put(k,FluidPlotterPoint(k,v[0],v[1],Color.black,1));
		});
		if(userView.notNil,{
			this.refresh;
		});
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
		arg identifier;
		highlightIdentifier = identifier;
		this.refresh;
	}

	dictNotProperlyFormatted {
		"FluidPlotter: The dictionary passed in is not properly formatted.".error;
	}

	createPlotWindow {
		arg bounds,parent_, mouseMoveAction,dict_;
		var xpos, ypos;
		var mouseAction = {
			arg view, x, y, modifiers, buttonNumber, clickCount;
			x = x.linlin(pointSize/2,userView.bounds.width-(pointSize/2),xmin,xmax);
			y = y.linlin(pointSize/2,userView.bounds.height-(pointSize/2),ymax,ymin);
			mouseMoveAction.(this,x,y,modifiers,buttonNumber, clickCount);
		};

		if(parent_.isNil,{xpos = 0; ypos = 0},{xpos = bounds.left; ypos = bounds.top});
		{
			parent = parent_ ? Window("FluidPlotter",bounds);
			userView = UserView(parent,Rect(xpos,ypos,bounds.width,bounds.height));

			userView.drawFunc_({
				if(dict_internal.notNil,{
					dict_internal.keysValuesDo({
						arg key, pt;
						var pointSize_, scaledx, scaledy, color;

						/*				key.postln;
						pt.postln;
						pt.x.postln;
						pt.y.postln;*/

						if(key == highlightIdentifier,{
							pointSize_ = pointSize * 2.3 * pt.sizeMultiplier
						},{
							pointSize_ = pointSize * pt.sizeMultiplier
						});

						scaledx = pt.x.linlin(xmin,xmax,0,userView.bounds.width,nil) - (pointSize_/2);
						scaledy = pt.y.linlin(ymax,ymin,0,userView.bounds.height,nil) - (pointSize_/2);

						shape.switch(
							\square,{Pen.addRect(Rect(scaledx,scaledy,pointSize_,pointSize_))},
							\circle,{Pen.addOval(Rect(scaledx,scaledy,pointSize_,pointSize_))}
						);

						Pen.color_(pt.color);
						Pen.draw;
					});
				});
			});

			userView.mouseMoveAction_(mouseAction);
			userView.mouseDownAction_(mouseAction);

			this.background_(Color.white);

			if(parent_.isNil,{parent.front;});
		}.defer;
	}

	close {
		parent.close;
	}
}