s.reboot
~ds = FluidDataSet.new(s,\simple1data)
~point = Buffer.alloc(s,1,1)
(
Routine{
    10.do{|i|
        ~point.set(0,i);
        s.sync;
        ~ds.addPoint(i.asString,~point,{("addPoint"+i).postln})
    }
}.play
)

/*** KDTREE ***/
~tree = FluidKDTree.new(s)
~tree.fit(~ds,action:{"Done indexing".postln})

k = 5; //play with this
(
Routine{
    10.do{|i|
        ~point.set(0,i);
        s.sync;
        ("Neighbours for point" + i).postln;
        ~tree.kNearest(~point, k, {|x| ("Labels:" + x).postln})
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
        s.sync;
        ~kmeans.predictPoint(~point,{|x| ("Predicted Cluster for point" + i ++ ":" + x).postln})
    }
}.play
)

~labels = FluidLabelSet(s,\simple1label);

~kmeans.predict(~ds,~labels, {|x| ("Size of each cluster" +  x).postln})

(
Routine{
    var n;
    ~labels.size({|x|
		x.asInteger.do{|i|
        ~labels.getLabel(i.asString,action: {|l|("Label for" + i ++ ":" + l).postln});
			s.sync;
	};});
}.play
)
