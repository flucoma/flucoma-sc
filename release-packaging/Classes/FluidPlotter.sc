FluidPlotterPoint {
	var id, <x, <y, <>color, <>size = 1;

	*new {
		arg id, x, y, color(Color.black), size = 1;
		^super.newCopyArgs(id,x,y,color,size);
	}
}

FluidPlotter : FluidViewer {
	var <parent, <xmin, <xmax, <ymin, <ymax, standalone,
	<zoomxmin, <zoomxmax, <zoomymin, <zoomymax,
	<userView, <pointSize = 6, pointSizeScale = 1, dict_internal, <dict,
	shape = \circle, highlightIdentifiersArray, categoryColors;

	*new {
		arg parent, bounds, dict, mouseMoveAction,
		xmin = 0, xmax = 1, ymin = 0, ymax = 1, standalone = true;

		if (parent.notNil) { standalone = false };
		^super.newCopyArgs(parent, xmin, xmax, ymin, ymax, standalone)
		.init(bounds, dict, mouseMoveAction);
	}

	init {
		arg bounds, dict, mouseMoveAction;

		zoomxmin = xmin;
		zoomxmax = xmax;
		zoomymin = ymin;
		zoomymax = ymax;

		categoryColors = this.createCatColors;
		dict_internal = Dictionary.new;
		if (dict.notNil) { this.dict = dict };
		this.createPlotWindow(bounds, mouseMoveAction);
	}

	categories_ {
		arg labelSetDict;
		if(dict_internal.size != 0,{
			var label_to_int = Dictionary.new;
			var counter = 0;
			dict_internal.keysValuesDo({
				arg id, fp_pt;

				// the id has to be converted back into a string because the
				// labelSetDict that comes in has the keys as strings by default
				var category_string = labelSetDict.at("data").at(id.asString)[0];
				var category_int;
				var color;

				if(label_to_int.at(category_string).isNil,{
					label_to_int.put(category_string,counter);
					counter = counter + 1;
				});

				category_int = label_to_int.at(category_string);

				if(category_int > (categoryColors.size-1),{
					"FluidPlotter:setCategories_ FluidPlotter doesn't have that many category colors. You can use the method 'setColor_' to set colors for individual points.".warn
				});

				color = categoryColors[category_int];
				fp_pt.color_(color);
			});
			this.refresh;
		},{
			"FluidPlotter::setCategories_ FluidPlotter cannot receive method \"categories_\". It has no data. First set a dictionary.".warn;
		});
	}

	pointSize_ {
		arg identifier, size;
		identifier = identifier.asSymbol;
		if(dict_internal.at(identifier).notNil,{
			dict_internal.at(identifier).size_(size);
			this.refresh;
		},{
			"FluidPlotter::pointSize_ identifier not found".warn;
		});
	}

	addPoint_ {
		arg identifier, x, y, color, size = 1;
		identifier = identifier.asSymbol;
		if(dict_internal.at(identifier).notNil,{
			"FluidPlotter::addPoint_ There already exists a point with identifier %. Point not added. Use setPoint_ to overwrite existing points.".format(identifier).warn;
		},{
			this.setPoint_(identifier,x,y,color,size);
		});
	}

	setPoint_ {
		arg identifier, x, y, color, size = 1;

		identifier = identifier.asSymbol;

		dict_internal.put(identifier,FluidPlotterPoint(identifier,x,y,color ? Color.black,size));

		this.refresh;
	}

	pointColor_ {
		arg identifier, color;
		identifier = identifier.asSymbol;
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
			dict_internal.put(k.asSymbol,FluidPlotterPoint(k,v[0],v[1],Color.black,1));
		});
		if(userView.notNil,{
			this.refresh;
		});
	}

	xmin_ {
		arg val;
		xmin = val;
		zoomxmin = xmin;
		this.refresh;
	}

	xmax_ {
		arg val;
		xmax = val;
		zoomxmax = xmax;
		this.refresh;
	}

	ymin_ {
		arg val;
		ymin = val;
		zoomymin = ymin;
		this.refresh;
	}

	ymax_ {
		arg val;
		ymax = val;
		zoomymax = ymax;
		this.refresh;
	}

	highlight_ {
		arg identifier;

		if(identifier.isKindOf(String).or(identifier.isKindOf(Symbol)),{identifier = [identifier]});

		highlightIdentifiersArray = identifier.collect({arg item; item.asSymbol});
		this.refresh;
	}

	dictNotProperlyFormatted {
		"FluidPlotter: The dictionary passed in is not properly formatted.".error;
	}

	createPlotWindow {
		arg bounds, mouseMoveAction;
		var zoomRect = nil;
		var zoomDragStart = Point(0,0);

		bounds = bounds ? Rect(0,0,800,800);
		if (parent.isNil) {
			if (standalone) {
				parent = Window("FluidPlotter", bounds);
				userView = UserView();
				defer {
					parent.view.layout = HLayout(userView).margins_(0).spacing_(0);
				}
			} {
				parent = userView = UserView();
			}
		} {
			userView = UserView(parent, bounds)
		};

		{
			var reportMouseActivity;

			userView.drawFunc = {
				arg viewport;
				var w = viewport.bounds.width, h = viewport.bounds.height;
				if(dict_internal.notNil,{
					dict_internal.keysValuesDo({
						arg key, pt;
						var pointSize_, scaledx, scaledy, color;

						pointSize_ = pointSize * pt.size;
						if (highlightIdentifiersArray.notNil) {
							if (highlightIdentifiersArray.includes(key)) {
								pointSize_ = pointSize_ * 2.3;
							};
						};
						pointSize_ = pointSize_ * pointSizeScale;

						scaledx = pt.x.linlin(zoomxmin,zoomxmax,0,w,nil) - (pointSize_/2);
						scaledy = pt.y.linlin(zoomymax,zoomymin,0,h,nil) - (pointSize_/2);

						shape.switch(
							\square, {
								Pen.addRect(Rect(scaledx,scaledy,pointSize_,pointSize_))
							},
							\circle, {
								Pen.addOval(Rect(scaledx,scaledy,pointSize_,pointSize_))
							}
						);

						Pen.color_(pt.color);
						Pen.draw;
					});

					if(zoomRect.notNil,{
						Pen.strokeColor_(Color.black);
						Pen.addRect(zoomRect);
						Pen.draw(2);
					});
				});
			};

			reportMouseActivity = {
				arg view, x, y, modifiers, buttonNumber, clickCount;
				var realx = x.linlin(pointSize/2,userView.bounds.width-(pointSize/2),zoomxmin,zoomxmax);
				var realy = y.linlin(pointSize/2,userView.bounds.height-(pointSize/2),zoomymax,zoomymin);
				mouseMoveAction.(this,realx,realy,modifiers,buttonNumber, clickCount);
			};

			userView.mouseDownAction = {
				arg view, x, y, modifiers, buttonNumber, clickCount;
				case { modifiers.isAlt } {
					zoomDragStart.x = x;
					zoomDragStart.y = y;
					zoomRect = Rect(zoomDragStart.x,zoomDragStart.y,0,0);
				}
				{ modifiers.isCtrl } {
					this.resetZoom;
				}
				{
					reportMouseActivity.(this,x,y,modifiers,buttonNumber,clickCount);
				};
			};

			userView.mouseMoveAction = {
				arg view, x, y, modifiers, buttonNumber, clickCount;
				if (modifiers.isAlt) {
					zoomRect = Rect(zoomDragStart.x,zoomDragStart.y,x - zoomDragStart.x,y - zoomDragStart.y);
					this.refresh;
				} {
					reportMouseActivity.(this,x,y,modifiers,buttonNumber,clickCount);
				};
			};

			userView.mouseUpAction = {
				arg view, x, y, modifiers, buttonNumber, clickCount;

				if(zoomRect.notNil,{
					var xmin_new, xmax_new, ymin_new, ymax_new;

					zoomRect = nil;

					xmin_new = min(x,zoomDragStart.x).linlin(0,userView.bounds.width,zoomxmin,zoomxmax,nil);
					xmax_new = max(x,zoomDragStart.x).linlin(0,userView.bounds.width,zoomxmin,zoomxmax,nil);

					// it looks like maybe these are wrong, with max on top and min on bottom, but they are
					// correct. this accounts for the fact that for the pixels, the lower numbers are higher
					// in the frame and vice versa, but for the plot values the lower numbers are lower in
					// the window.
					ymin_new = max(y,zoomDragStart.y).linlin(userView.bounds.height,0,zoomymin,zoomymax,nil);
					ymax_new = min(y,zoomDragStart.y).linlin(userView.bounds.height,0,zoomymin,zoomymax,nil);

					zoomxmin = xmin_new;
					zoomxmax = xmax_new;
					zoomymin = ymin_new;
					zoomymax = ymax_new;

					this.refresh;
				});

				reportMouseActivity.(this,x,y,modifiers,buttonNumber,clickCount);
			};

			this.background_(Color.white);

			if (standalone) { parent.front };
		}.defer;
	}

	asView { ^userView }

	resetZoom {
		zoomxmin = xmin;
		zoomxmax = xmax;
		zoomymin = ymin;
		zoomymax = ymax;
		this.refresh;
	}

	post {
		"xmin:      %".format(xmin).postln;
		"xmax:      %".format(xmax).postln;
		"ymin:      %".format(ymin).postln;
		"ymax:      %".format(ymax).postln;
		"zoomxmin: %".format(zoomxmin).postln;
		"zoomxmax: %".format(zoomxmax).postln;
		"zoomymin: %".format(zoomymin).postln;
		"zoomymax: %".format(zoomymax).postln;
	}

	close {
		parent.close;
	}
}
