s.reboot


~urn = { |n=31416, min=0,max=31415| (min..max).scramble.keep(n) };

n = 200
~idx = ~urn.value(n)
~data = n.collect{|i|sin(~idx[i]/5000)}
(
~simpleInput = FluidDataSet(s,\simpleInput,1);
~simpleOutput = FluidDataSet(s,\simpleOutput,1);
b = Buffer.alloc(s,1,1);
~mappingviz = Buffer.alloc(s,31416,1);
)
(
Routine{
n.do{|i|
        b.set(0,~idx[i]);
        s.sync;
        ~simpleInput.addPoint(i.asString,b,{("Added Input" + i).postln});
        b.set(0,~data[i]);
        s.sync;
        ~simpleOutput.addPoint(i.asString,b,{("Added Output" + i).postln});
        ~mappingviz.set(~idx[i].asInt,~data[i])
    }
}.play
)
(
~simpleInput.clear;
~simpleOutput.clear;
)
3%2
~mappingviz.plot(minval:-1,maxval:1)

~mappingresult = Buffer.alloc(s,31416,1);

~knn = FluidKNN(s)
~knn.index(~simpleInput,action:{"index done".postln})
(

    512.do{|i|
        b.set(0,i);
        ~knn.regress(b,~simpleOutput,1,action:{|d|~mappingresult.set(i,d)});
    // if(i%512 == 0, {i.postln; s.sync},{});
}
)