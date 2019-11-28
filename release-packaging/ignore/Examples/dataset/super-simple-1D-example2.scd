s.reboot
~ds = FluidDataSet.new(s,\simple1data,1)
~point = Buffer.alloc(s,1,1)
(
Routine{
    10.do{|i|
        var d;
        if(i<=4,{d=i},{d=i+5});
        ~point.set(0,d);
        s.sync;
        ~ds.addPoint(i.asString,~point,{("addPoint"+i).postln})
    }
}.play
)

/*** KDTREE ***/
~tree = FluidKDTree.new(s)
~tree.index(~ds,action:{"Done indexing".postln})

k = 5; //play with this
(
Routine{
    10.do{|i|
        ~point.set(0,i*10);
        s.sync;
        ("Neighbours for point" + (i*10)).postln;
        ~tree.kNearest(~point, k, {|x| ("Labels:" + x).postln});
        ~tree.kNearestDist(~point,k,{|x| ("Distances:" + x).postln})
    }
}.play
)

/*** KMEANS ***/

~kmeans = FluidKMeans.new(s)
~nClusters = 2; //play with this
~kmeans.fit(~ds,~nClusters,100,action:{"Done fitting".postln})
(
Routine{
    10.do{|i|
        ~point.set(0,i * 10);
        s.sync;
        ~kmeans.predictPoint(~point,{|x| ("Predicted Cluster for point" + (i * 10) ++ ":" + x).postln})
    }
}.play
)

~labels = FluidLabelSet(s,\simple1label);

~kmeans.predict(~ds,~labels, {|x| ("Size of each cluster" +  x).postln})
(
Routine{
    var n;
    ~labels.size({|x| n = x.asInt});
    n.asInt.do{|i|
        ~labels.getLabel(i.asString,action: {|l|("Label for" + i ++ ":" + l).postln});
    }
}.play
)


