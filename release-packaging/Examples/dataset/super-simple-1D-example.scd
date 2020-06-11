s.reboot
~ds = FluidDataSet.new(s,\simple1data)
~point = Buffer.alloc(s,1,1)
(
Routine{
	10.do{|i|
		~point.set(0,i);
		~ds.addPoint(i.asString,~point,{("addPoint"+i).postln});
		s.sync;
	}
}.play
)
~ds.print;

/*** KDTREE ***/
~tree = FluidKDTree.new(s)
~tree.fit(~ds,action:{"Done indexing".postln})

k = 5; //play with this
(
Routine{
	10.do{|i|
		~point.set(0,i);
		~tree.kNearest(~point, k, {|x| "Neighbours for a value of % are ".postf(i); x.postln});
		s.sync;
	}
}.play
)

/*** KMEANS ***/

~kmeans = FluidKMeans.new(s)
~nClusters = 2; //play with this
~kmeans.fit(~ds,~nClusters,100,action:{|x| "Done fitting with these number of items per cluster ".post;x.postln;})

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
	Routine{x.asInteger.do{|i| //size does not return a value, but we can retrieve it via a function
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