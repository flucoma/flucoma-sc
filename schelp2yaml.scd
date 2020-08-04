//Run this block to set up, then get yersel down to line 194 ish
(
~processLink = { |node|
	var l = node.text;

	var result = case
	{"^http".matchRegexp(l)}            {l}
	{"^Classes/Buffer".matchRegexp(l)}  {"|buffer|"}
	{"^Classes/Fluid.*".matchRegexp(l)} {":fluid-obj:`"++l.findRegexp("^Classes/(Fluid.*)").last[1]++"`"}
	{"^Guides/Fluid.*".matchRegexp(l)}  {":fluid-topic:`"++l.findRegexp("^Guides/(Fluid.*)").last[1]++"`"};
	result ?? {result = "`"++l++"`"};
	result
};

~processList = {|node,indent|
	var ind = "".catList(indent.collect{" "});
	var result = "";
	node.children.do{|i|
		var itemText = "".catList(i.findChild(\PROSE).children.collect{|child| child.text});
		result = result ++ ind ++ "* " ++ itemText ++ "\n";
	};
	result
};

~processTable = { |node, indent|
	var ind = "".catList(indent.collect{" "});
	var result;
	var longestCell = 0;
	var rows = node.children.collect{|row|
		row.children.collect{|col|
			 var cell = "".catList(col.findChild(\PROSE).children.collect{|child|child.text});
			longestCell = longestCell.max(cell.size);
			cell
		}
	};

	var divider = "".catList(longestCell.collect{"="});
	var nCols = rows.collect{|col| col.size}.maxItem;
	var head = nCols.collect{divider}.join("  ");
	result = ind ++ head ++ "\n";
	rows.do{|r| result = result ++ ind ++ r.join("  ") ++ "\n"};
	result = result ++ ind ++ head ++ "\n";
	result
};

~rstify = { |node, indent = 0|
	var result;
	var ind = indent.collect(" ").join("");
	switch(node.id,
		\CODE, {result = "`"++node.text++"`"},
		\STRONG, {result = "**"++node.text++"**"},
		\EMPHASIS, {result = "*"++node.text++"*" },
		\LIST, {result = ~processList.value(node,indent + 1)},
		\TEXT, {result =  node.text },
		\LINK, {result = ~processLink.value(node)},
		\TABLE, {result = ~processTable.value(node,indent + 1)},
		\PROSE, {
			if(node.children.size > 0)
			{ result =  ind ++ node.children.collect{|n|~rstify.value(n,indent)}.join("")}
			{ result = ""}
		}
	);
	result
};

~boilerplate  = {|stream|
	stream << "# Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)\n"
	<< "# Copyright 2017-2019 University of Huddersfield.\n"
	<< "# Licensed under the BSD-3 License.\n"
	<< "# See license.md file in the project root for full license information.\n"
	<< "# This project has received funding from the European Research Council (ERC)\n"
	<< "# under the European Union’s Horizon 2020 research and innovation programme\n"
	<< "# (grant agreement No 725899).\n"
	<< "---\n";
};

~metadata = { |d, stream|
	var title = d.findChild(\TITLE);
	var digest = d.findChild(\SUMMARY);
	var categories = d.findChild(\CATEGORIES);
	var related = d.findChild(\RELATED);
	stream << "digest: " << digest.text << "\n";
	stream << "sc-categries: ";
	categories !? {
		var c = categories.children.collect{|child| child.text};
		stream << c.join(", ");
	};
	stream << "\n";
	stream << "sc-related: ";
	related !? {
		var r = related.children.collect{|child| child.text};
		stream << r.join(", ");
	};
	stream << "\n";
	stream << "see-also: \n"; //placeholder
};

~objectdesc = { |d, stream|
	var desc = d.findChild(\BODY).findChild(\DESCRIPTION);
	stream << "description: |\n";
	desc.children.do{|p| //expecting each child to be a prose block?
		stream << ~rstify.value(p,2);
		stream << "\n\n";
	};
	// stream << "\n";
};



~params = { |d, stream|
	var args = d.findChild(\ARGUMENTS);
	var valid = args.children.collect{|a| a.text.asSymbol}.difference([\server,\in,\trig,\action]).size;
	if(valid > 0)
	{
		stream << "parameters:\n";
		args.children.do{|a|
			var p = a.findChild(\PROSE);
			[\server,\in,\trig,\action].indexOf(a.text.asSymbol) ?? {
				stream << "  " << a.text << ":\n    description: |\n";
				stream << ~rstify.value(p,6);
				stream << "\n";
			}
		}
	}
};

~messages = { |d, stream|
	var messageNodes = d.findChild(\BODY).findChild(\INSTANCEMETHODS);
	messageNodes !? {
		stream << "messages: \n";
		messageNodes.children.do {|method|
			var names = method.findChild(\METHODNAMES);
			var messageDesc = method.findChild(\METHODBODY) !? (_.findChild(\PROSE));
			var messageArgs = method.findChild(\METHODBODY) !? (_.findChild(\ARGUMENTS));

			names !? {
				names.children.collect{|child|child.text}.do{|name|
					stream << "  " << name << ":\n";
					stream << "   " << "description: |\n";
					stream << ~rstify.value(messageDesc,6) << "\n";
					stream << "   args:\n";
					messageArgs !? {
						messageArgs.children.do{|a|
							var argName = a.text;
							stream << "    - name: " << argName << "\n";
							stream << "      " << "description: |\n";
							stream << ~rstify.value(a.findChild(\PROSE),8) << "\n";
						}
					}
				}
			}
		}
	}
};

~locateParamsMethod = { |class|
	var method = case
	{class.superclasses.indexOf(FluidManipulationClient).notNil}{"*new"}
	{"^FluidBuf.*".matchRegexp(class.name.asString)}{"*process"}
	{"^Fluid.*".matchRegexp(class.name.asString)}{
		case
		{class.respondsTo(\ar)}{"*ar"}
		{class.respondsTo(\kr)}{"*kr"}
	};
	SCDoc.getMethodDoc(class.name, method).findChild(\METHODBODY);
};

~examplecode = { |d,stream|
	var eg = d.findChild(\BODY).findChild(\EXAMPLES);

	eg !? {
		eg.children.do{|node|
			switch(node.id,
				\CODEBLOCK,{ stream << "::\n  " << node.text << "\n" },
				\PROSE,{ stream << ~rstify.value(node) << "\n" }
			);
		}
	}
};

~schelp2yaml={|class,outpath|
	var className = class.name;
	var fileName = className.asString.findRegexp("Fluid(.*)").last[1];
	var helpSourcePath = SCDoc.documents["Classes"+/+className].fullPath;
	var f = File.open(outpath+/+fileName++".yaml","w");
	var d = SCDoc.parseFileFull(helpSourcePath);
	var m = ~locateParamsMethod.value(class);
	~boilerplate.value(f);
	~metadata.value(d.findChild(\HEADER),f);
	~objectdesc.value(d,f);
	~params.value(m,f);
	~messages.value(d,f);
	// ~examplecode.value(d,f);
	f.close;
	("Written" + outpath+/+fileName++".yaml").postln;
};
)
//Conviniences for batch-getting our classes by species
~allFluid = Class.allClasses.select({|x| "^Fluid.*".matchRegexp(x.name.asString)})
~fluidnrt = ~allFluid.select{|x| "FluidBuf.*".matchRegexp(x.name.asString)};
~manip = ~allFluid.select{|x| x.superclasses.indexOf(FluidManipulationClient) != nil} ;
~rt = ~allFluid.difference(~fluidnrt).difference(~manip);

//Make yaml files like this
~schelp2yaml.value(FluidKMeans,"/tmp") ;

(
	[FluidKDTree,
	FluidKMeans,
	FluidKNNClassifier,
	FluidKNNRegressor,
	FluidNormalize,
	FluidStandardize,
	FluidPCA].do{|class| ~schelp2yaml.value(class,"/tmp") };
)

//The Misssing Ones:
(
[
	FluidBufFlatten,
	// FluidNNDSVD,
	FluidBufNMFCross,
	FluidAudioTransport,
	FluidBufAudioTransport,
	FluidDataSet,
	FluidLabelSet,
	FluidKDTree,
	FluidKMeans,
	FluidKNNClassifier,
	FluidKNNRegressor,
	FluidNormalize,
	FluidStandardize,
	FluidPCA,
	FluidMDS,
	FluidDataSetQuery,
	FluidMLPRegressor
].do{|class| ~schelp2yaml.value(class,"/tmp") };
)

(
[FluidDataSet,FluidDataSetQuery,FluidKDTree,FluidKMeans,FluidKNNClassifier,FluidKNNRegressor,FluidMDS,FluidMLPRegressor,FluidNormalize,FluidPCA,FluidStandardize].do{|class| class.postln;~schelp2yaml.value(class,"/tmp") };
)
