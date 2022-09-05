FluidProcessSelect {
	var processClass, options;
	var lookupDict, defaultSelection, defaultBits;

	*new { |processClass, options, defaultSelection|
		^super.newCopyArgs(processClass, options).init(defaultSelection)
	}

	init { |default|
		lookupDict = Dictionary.with(*options.collect{|x,i| x->(1<<i)});
		defaultSelection = (default ? options).asArray;
		defaultBits = this.encode(defaultSelection);
	}

	encode {|a|
		var bits;
		a = (a ?? defaultSelection).asArray;
		a.asBag.countsDo{ |item, count, i|
			if(count > 1) { "Option '%' is repeated".format(item).warn };
		};
		bits = a.collect{ |sym|
			(lookupDict[sym.asSymbol] ?? { this.prWarnUnrecognised(sym); 0 })
		}.reduce{ |x, y| x | y };
		^bits ?? defaultBits
	}

	prWarnUnrecognised {|sym|
		"% - '%' is not a recognised option".format(processClass, sym).warn
	}
}
