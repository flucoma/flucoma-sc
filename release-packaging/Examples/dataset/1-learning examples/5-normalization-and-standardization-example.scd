(
// set some variables
~nb_of_dim = 10;
~dataset = FluidDataSet(s);
)

(
// fill up the dataset with 20 entries of 10 column/dimension/descriptor value each. The naming of the item's label is arbitrary as usual
Routine{
	var buf = Buffer.alloc(s,~nb_of_dim);
	20.do({ arg i;
		buf.loadCollection(Array.fill(~nb_of_dim,{rrand(0.0,100.0)}));
		~dataset.addPoint("point-"++i.asInteger.asString, buf);
		s.sync;
	});
	buf.free;
	\done.postln;
}.play
)

~dataset.print;

// make a buf for getting points back
~query_buf = Buffer.alloc(s,~nb_of_dim);

// look at a point to see that it has points in it
~dataset.getPoint("point-0",~query_buf,{~query_buf.getn(0,~nb_of_dim,{|x|x.postln;});});

// look at another point to make sure it's different...
~dataset.getPoint("point-7",~query_buf,{~query_buf.getn(0,~nb_of_dim,{|x|x.postln;});});

///////////////////////////////////////////////////////
// exploring full dataset normalization and standardization

// make a FluidNormalize
~normalize = FluidNormalize(s,0,1);

// fits the dataset to find the coefficients
~normalize.fit(~dataset,{"done".postln;});

// making an empty 'normed_dataset' which is required for the normalize function
~normed_dataset = FluidDataSet(s);

// normalize the full dataset
~normalize.transform(~dataset,~normed_dataset,{"done".postln;});

// look at a point to see that it has points in it
~normed_dataset.getPoint("point-0",~query_buf,{~query_buf.getn(0,~nb_of_dim,{|x|x.postln;});});
// 10 numbers between 0.0 and 1.0 where each column/dimension/descriptor is certain to have at least one item on which it is 0 and one on which it is 1
// query a few more for fun

// try FluidStandardize
~standardize = FluidStandardize(s);

// fits the dataset to find the coefficients
~standardize.fit(~dataset,{"done".postln;});

// standardize the full dataset
~standardized_dataset = FluidDataSet(s);
~standardize.transform(~dataset,~standardized_dataset,{"done".postln;});

// look at a point to see that it has points in it
~standardized_dataset.getPoint("point-0",~query_buf,{~query_buf.getn(0,~nb_of_dim,{|x|x.postln;});});
// 10 numbers that are standardize, which mean that, for each column/dimension/descriptor, the average of all the points will be 0. and the standard deviation 1.

////////////////////////////////////////////////////
// exploring point querying  concepts via norm and std

// Once a dataset is normalized / standardized, query points have to be scaled accordingly to be used in distance measurement. In our instance, values were originally between 0 and 100, and now they will be between 0 and 1 (norm), or their average will be 0. (std). If we have data that we want to match from a similar ranging input, which is usually the case, we will need to normalize the searching point in each dimension using the same coefficients.

// first, make sure you have run all the code above, since we will query these datasets

// get a know point as a query point
~dataset.getPoint("point-7",~query_buf);

// find the 2 points with the shortest distances in the dataset
~tree = FluidKDTree.new(s,numNeighbours:2);
~tree.fit(~dataset)
~tree.kNearest(~query_buf, {|x| ("Labels:" + x).postln});
~tree.kNearestDist(~query_buf, {|x| ("Distances:" + x).postln});
// its nearest neighbourg is itself: it should be itself and the distance should be 0. The second point is depending on your input dataset.

// normalise that point (~query_buf) to be at the right scale
~normbuf = Buffer.alloc(s,~nb_of_dim);
~normalize.transformPoint(~query_buf,~normbuf);
~normbuf.getn(0,~nb_of_dim,{arg vec;vec.postln;});

// make a tree of the normalized database and query with the normalize buffer
~normtree = FluidKDTree.new(s,numNeighbours:2);
~normtree.fit(~normed_dataset)
~normtree.kNearest(~normbuf,  {|x| ("Labels:" + x).postln});
~normtree.kNearestDist(~normbuf, {|x| ("Distances:" + x).postln});
// its nearest neighbourg is still itself as it should be, but the 2nd neighbourg might have changed. The distance is now different too

// standardize that same point (~query_buf) to be at the right scale
~stdbuf = Buffer.alloc(s,~nb_of_dim);
~standardize.transformPoint(~query_buf,~stdbuf);
~stdbuf.getn(0,~nb_of_dim,{arg vec;vec.postln;});

// make a tree of the standardized database and query with the normalize buffer
~stdtree = FluidKDTree.new(s, numNeighbours: 2);
~stdtree.fit(~standardized_dataset)
~stdtree.kNearest(~stdbuf, {|x| ("Labels:" + x).postln});
~stdtree.kNearestDist(~stdbuf, {|x| ("Distances:" + x).postln});
// its nearest neighbourg is still itself as it should be, but the 2nd neighbourg might have changed yet again. The distance is also different too

// where it starts to be interesting is when we query points that are not in our original dataset

// fill with known values (50.0 for each of the 10 column/dimension/descriptor, aka the theoretical middle point of the multidimension space) This could be anything but it is fun to aim in the middle.
~query_buf.fill(0,~nb_of_dim,50);

// normalize and standardize the query buffer. Note that we do not need to fit since we have not added a point to our reference dataset
~normalize.transformPoint(~query_buf,~normbuf);
~standardize.transformPoint(~query_buf,~stdbuf);

//query the single nearest neighbourg via 3 different data scaling. Depending on the random source at the begining, you should get (small or large) differences between the 3 answers!
[~tree,~normtree,~stdtree].do{|t| t.numNeighbours =1  };
~tree.kNearest(~query_buf, {|x| ("Original:" + x).post;~tree.kNearestDist(~query_buf, {|x| (" with a distance of " + x).postln});});
~normtree.kNearest(~normbuf, {|x| ("Normalized:" + x).post;~normtree.kNearestDist(~normbuf, {|x| (" with a distance of " + x).postln});});
~stdtree.kNearest(~stdbuf, {|x| ("Standardized:" + x).post; ~stdtree.kNearestDist(~stdbuf, {|x| (" with a distance of " + x).postln});});
