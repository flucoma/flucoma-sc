s.reboot

~urn = { |n=31416, min=0,max=31415| (min..max).scramble.keep(n) };

// creates 200 indices, then values of the output of a fundion with a predictable shape of a sinewave
n = 200
~idx = ~urn.value(n)
~data = n.collect{|i|sin(~idx[i]/5000)}

// creates the dataset with these associated indices and values
(
~simpleInput = FluidDataSet(s);
~simpleOutput = FluidDataSet(s);
b = Buffer.alloc(s,1);
c = Buffer.alloc(s,1);
~mappingviz = Buffer.alloc(s,512);
)

(
Routine{
    n.do{|i|
        b.set(0,~idx[i]);
        c.set(0,~data[i]);
        ~simpleInput.addPoint(i.asString,b,{("Added Input" + i).postln});
        ~simpleOutput.addPoint(i.asString,c,{("Added Output" + i).postln});
        ~mappingviz.set((~idx[i]/61).asInteger,~data[i]);
        s.sync;
    }
}.play
)

~simpleInput.print
~simpleOutput.print

//look at the seeing material
~mappingviz.plot(minval:-1,maxval:1)

//create a buffer to query
~mappingresult = Buffer.alloc(s,512);

//make the process then fit the data
~knn = FluidKNNRegressor(s,3,1)
~knn.fit(~simpleInput, ~simpleOutput, action:{"fitting done".postln})

// query 512 points along the line (slow because of all that sync'ing)
(
~knn.numNeighbours = 1; // change to see how many points the system uses to regress
Routine{
	512.do{|i|
		b.set(0,i*61);
		~knn.predictPoint(b,action:{|d|~mappingresult.set(i,d);});
		s.sync;
		i.postln;
	}
}.play
)

// look at the interpolated values
~mappingresult.plot
