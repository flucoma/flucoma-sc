FluidDataSet(s,\bob)


~assert = { |condition,msg| if(condition){("Condition Met:" + msg).postln}{msg.error}};

~assert.value(false,"Ohono")
~assert
a = Synth(\FluidDataSet1054)
a.free
OSCFunc({|m|m.postln}).oneShot
OSCFunc.trace(false);
~assert.value(
ServerStatusWatcher
//Server running sanity check
//1. create before running, wait and see if we have a valid instance to call
(
		~foo = FluidDataSet(s,\foo);
		~waitForCounts = Condition.new;
		~countsListener = { |s,changed|
			if(changed == \counts) {
				~waitForCounts.test = true;
				~waitForCounts.signal;
			}
		};
		s.waitForBoot{
			fork{
				protect{
					s.addDependant(~countsListener);
					~waitForCounts.wait;
					s.removeDependant(~countsListener);
					~assert.value(s.numSynths == 1,"One synth after deferred startup" );
/*		~testPoint = Buffer.alloc(s,8);
		s.sync;
		~testPoint.setn(0,[1,2,3,4,5,6,7,8]);
		s.sync;
		~foo.addPoint(\one,~testPoint);*/
				}{
					~foo.free;
					s.quit;
				};
			}
		}
)





//Uniqueness: Dataset and Labelset should complain if you try to
//reuse a name (but not otherwise!)
//1. no server running (will get warnings)
(
~foo = FluidDataSet(s,\foo); //obviously, this should work
try{ ~bar = FluidDataSet(s,\foo)}{|error|
	if(error.class == FluidDataSetExistsError)
	{"FluidDataSetExistsError, as expected".postln}{"Oh no".error}
}; //we expect a FluidDataSetExistsError here
~bar = FluidDataSet(s,\bar); //But this should work (different name)
~bar.free;
~foo.free;
~foo = FluidDataSet(s,\foo); //and we should be able to reuse a name once it's available again
~foo.free;
)

//2. with server (can verify same instances in synth are actually being use)
(
s.waitForBoot{
	fork{
		~testPoint = Buffer.alloc(s,8);
		s.sync;
		~testPoint.setn(0,[1,2,3,4,5,6,7,8]);
		s.sync;
		~foo = FluidDataSet(s,\foo); //obviously, this should work
		try{ ~bar = FluidDataSet(s,\foo)}{|error|
			if(error.class == FluidDataSetExistsError)
			{"FluidDataSetExistsError, as expected".postln}{"Oh no".error}
		}; //we expect a FluidDataSetExistsError here
		s.sync;
		~foo.addPoint(\one,~testPoint);
		s.sync;
/*		~bar = FluidDataSet(s,\bar); //But this should work (different name)
		s.sync;
		//Bar should still been size 0, cols 0 (i.e. no accidental aliasing)
		~bar.cols({|x| if(x == 0){"0 cols as expected".postln;}{"Wrong number of cols".error}});
		~bar.size({|x| if(x == 0){"0 size as expected".postln;}{"Wrong size".error}});
		s.sync;
		~bar.free;
		s.sync;
		//If we do a lookup, does it work?
		~bar = FluidDataSet.at(s,\foo);
		~bar.cols({|x| if(x == 8){"8 cols as expected".postln;}{"Wrong number of cols".error}});
		~bar.size({|x| if(x == 1){"1 size as expected".postln;}{"Wrong size".error}});
		s.sync;
		~bar.free;*/
		s.quit
	}
}
)


b = FluidDataSet(s,\bob)
b.free
c = Buffer.alloc(s,8)
c.setn(0,[1,2,3,4,5,6,7,8])
b.clear
b.addPoint(\one,c)
b.size({|size| size.postln})
b.cols({|cols| cols.postln})
d = Buffer.new
b.getPoint(\one,d)
d.getn(0,8,{|a| a.postln})

~another = FluidDataSet(s,\sue)
~another.cols({|size| size.postln})
~another.addPoint(\one,c)
\sue.asString
~tree = FluidKDTree(s)
~tree.fit(\bob)
~tree.cols({|cols| cols.postln})
~tree.free
ServerTree.removeAll
ServerBoot.removeAll
[3,89,111,89].asSymbol



ServerTree.objects.do{|x| x.do { |y| y.def.postln}}

ServerBoot.objects.do{|x| x.do {|y| if(y.class == Function){y.def.postln}    }}
ServerBoot.removeAll
ServerQuit.objects.do{|x| x.do { |y| y.def.postln}}