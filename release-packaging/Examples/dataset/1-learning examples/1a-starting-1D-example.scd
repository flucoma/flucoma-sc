s.reboot
~ds = FluidDataSet.new(s)
~point = Buffer.alloc(s,1,1)
(
Routine{
	10.do{|i|
		~point.set(0,i);
		~ds.addPoint(i.asString,~point,{("addPoint"+i).postln}); //because buffer.set do an immediate update in the RT thread we can take for granted it'll be updated when we call addPoint
		s.sync; //but we need to sync to make sure everything is done on the DataSet before the next iteration
	}
}.play
)
~ds.print;

/*** KDTREE ***/
~tree = FluidKDTree.new(s)
~tree.fit(~ds,action:{"Done indexing".postln})

~tree.numNeighbours = 5; //play with this
(
Routine{
	10.do{|i|
		~point.set(0,i);
		~tree.kNearest(~point, {|x| "Neighbours for a value of % are ".postf(i); x.postln});
		s.sync;
	}
}.play
)

/*** KMEANS ***/

~kmeans = FluidKMeans.new(s,maxIter:100);
~kmeans.numClusters = 2; //play with this
~kmeans.fit(~ds,action:{|x| "Done fitting with these number of items per cluster ".post;x.postln;})

(
Routine{
	10.do{|i|
		~point.set(0,i);
		~kmeans.predictPoint(~point,{|x| ("Predicted Cluster for a value of " + i ++ ":" + x).postln});
		s.sync;
	}
}.play
)

~labels = FluidLabelSet(s,\simple1label);

~kmeans.predict(~ds,~labels, {|x| ("Size of each cluster" +  x).postln})

(
~labels.size{|x|
	Routine{x.asInteger.do{|i|
		~labels.getLabel(i,action: {|l|
			("Label for entry " + i ++ ":" + l).postln;
		});
		s.sync;
	}
	}.play;
};
)

// or simply print it
~labels.print

// or dump and format
(
~labels.dump{|x|
	var keys = x["data"].keys.asArray.sort;
	keys.do{|key|
		"Label for entry % is %\n".postf(key, x["data"][key][0]);
	}
}
)
