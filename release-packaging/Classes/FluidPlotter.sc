FluidPlotterPoint {
	var id, <x, <y, <>color, <>size = 1;

	*new {
		arg id, x, y, color, size = 1;
		^super.new.init(id,x,y,color,size);
	}

	init {
		arg id_, x_, y_, color_, size_ = 1;
		id = id_;
		x = x_;
		y = y_;
		color = color_ ? Color.black;
		size = size_;
	}
}

FluidPlotter {
	var <parent, <userView, <xmin, <xmax, <ymin, <ymax, <pointSize = 6, pointSizeScale = 1, dict_internal, <dict, shape = \circle, catColors, highlightIdentifier;

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
		dict_internal = Dictionary.new;
		if(dict_.notNil,{this.dict_(dict_)});
		this.createPlotWindow(bounds,parent_,mouseMoveAction,dict_);
	}

	createCatColors {
		catColors = "1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf".clump(6).collect{
			arg six;
			Color(*six.clump(2).collect{
				arg two;
				"0x%".format(two).interpret / 255;
			});
		};
	}

	categories_ {
		arg labelSetDict;
		if(dict_internal.size != 0,{
			var label_to_int = Dictionary.new;
			var counter = 0;
			dict_internal.keysValuesDo({
				arg id, fp_pt;
				var category_string = labelSetDict.at("data").at(id)[0];
				var category_int;
				var color;

				if(label_to_int.at(category_string).isNil,{
					label_to_int.put(category_string,counter);
					counter = counter + 1;
				});

				category_int = label_to_int.at(category_string);

				if(category_int > (catColors.size-1),{
					"FluidPlotter:setCategories_ FluidPlotter doesn't have that many category colors. You can use the method 'setColor_' to set colors for individual points.".warn
				});

				color = catColors[category_int];
				fp_pt.color_(color);
			});
			this.refresh;
		},{
			"FluidPlotter::setCategories_ FluidPlotter cannot receive setCategories. It has no data. First set a dictionary.".warn;
		});
	}

	pointSize_ {
		arg identifier, size;
		if(dict_internal.at(identifier).notNil,{
			dict_internal.at(identifier).size_(size);
			this.refresh;
		},{
			"FluidPlotter::pointSize_ identifier not found".warn;
		});
	}

	// TODO: addPoint_ that checks if the key already exists and throws an error if it does
	addPoint_ {
		arg identifier, x, y, color, size = 1;
		if(dict_internal.at(identifier).notNil,{
			"FluidPlotter::addPoint_ There already exists a point with identifier %. Point not added. Use setPoint_ to overwrite existing points.".format(identifier).warn;
		},{
			this.setPoint_(identifier,x,y,size,color);
		});
	}

	setPoint_ {
		arg identifier, x, y, color, size = 1;

		dict_internal.put(identifier,FluidPlotterPoint(identifier,x,y,color ? Color.black,size));

		this.refresh;
	}

	pointColor_ {
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

	pointSizeScale_ {
		arg ps;
		pointSizeScale = ps;
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
							pointSize_ = pointSize * 2.3 * pt.size
						},{
							pointSize_ = pointSize * pt.size
						});

						pointSize_ = pointSize_ * pointSizeScale;

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