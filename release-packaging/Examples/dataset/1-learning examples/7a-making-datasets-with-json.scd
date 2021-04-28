// create the data dictionary
~data = Dictionary.new
7.do{|i| ~data.add(("entry-"++i).asSymbol -> 10.collect{|j|j/10 + i})}

// nest that dictionary in the dataset format, adding the number of columns
~dict = Dictionary.new
~dict.add(\data -> ~data)
~dict.add(\cols -> 10)

//create a dataset, then loading the dictionary
~ds =  FluidDataSet(s);
~ds.load(~dict)
~ds.print

//fun with kdtree to see it actually works
~kdtree = FluidKDTree.new(s,numNeighbours:5)
~kdtree.fit(~ds,{\done.postln;})
~target = Buffer.loadCollection(s,(4).dup(10));
~kdtree.kNearest(~target, {|a|a.postln;})
~kdtree.kNearestDist(~target, {|a|a.postln;})


/////////////////////////////////////////////
// creating a labelset the same way

// creating the data dictionary
~data2 = Dictionary.new
7.do{|i| ~data2.add(("entry-"++i).asSymbol -> (if( i.odd, {["odd"]},{["even"]})))}

// nesting again
~dict2 = Dictionary.new
~dict2.add(\data -> ~data2)
~dict2.add(\cols -> 1)

// creating a labelset and loading the dictionary
~ls =  FluidLabelSet(s);
~ls.load(~dict2)
~ls.print

// testin with a classifier toy example
~classifier = FluidKNNClassifier.new(s, numNeighbours:2);
~classifier.fit(~ds,~ls, {\done.postln;})

// run many times for random pleasure
(
fork{
	var value = 7.rand;
	~ds.getPoint(("entry-"++value).asSymbol,~target);
	s.sync;
	~classifier.predictPoint(~target, action: {|x|"entry % is an % entry.\n".postf(value,x);})
}
)