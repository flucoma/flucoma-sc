
TestFluidCorpusManipulationServer : UnitTest
{
	var waitForCounts, countsListener;

	setUp{
		waitForCounts = Condition.new(false);
		countsListener = { |s,changed|
			if(changed == \counts) {
				waitForCounts.test = true;
				waitForCounts.signal;
			}
		};
		Server.default.addDependant(countsListener);
	}

	tearDown{
		Server.default.removeDependant(countsListener);
		Server.default.quit;
	}

	test_DataSetPersistence{
		var foo, bar, tree, testPoint;

		foo = FluidDataSet(Server.default,\foo);

		this.bootServer(Server.default);
		while {Server.default.serverRunning.not}{0.2.wait};
		waitForCounts.test = false;
		waitForCounts.wait;

		this.assertEquals(Server.default.numSynths,1,"Dataset: One Synth present after deferred boot");

		waitForCounts.test = false;
		foo.free;
		Server.default.freeAll;
		waitForCounts.wait;

		this.assertEquals(Server.default.numSynths,0,"Dataset: One Synth present via cretation after boot");

		//Uniqueness test (difficult to run with previous instance of foo, because
		//UnitTest.bootServer messes with Server alloctors and screws up the ID cache
		foo = FluidDataSet(Server.default,\foo);
		this.assertException({
			bar = FluidDataSet(Server.default,\foo);
		},FluidDataSetExistsError,"DataSetDuplicateError on reused name", onFailure:{
			"Exception fail".postln;
		});

		waitForCounts.test = false;
		bar = FluidDataSet(Server.default,\bar);
		waitForCounts.wait;

		this.assertEquals(Server.default.numSynths,2,"Dataset: Two Synths present after new Dataset added");

		testPoint = Buffer.alloc(Server.default,8);
		Server.default.sync;
		testPoint.setn(0,[1,2,3,4,5,6,7,8]);
		Server.default.sync;
		foo.addPoint(\one,testPoint);
		Server.default.sync;
		foo.size({|size|
			this.assertEquals(size,1,"Dataset size is 1");
		});
		Server.default.sync;
		foo.cols({|cols|
			this.assertEquals(cols,8,"Dataset cols is 8");
		});

		Server.default.sync;
		waitForCounts.test = false;

		tree = FluidKDTree(Server.default);
		waitForCounts.wait;

		this.assert(tree.synth.notNil,"Tree should have a valid synth");
		this.assertEquals(Server.default.numSynths,3,"Dataset: Three Synths remain after cmd-.");

		tree.fit(foo);
		Server.default.sync;
		tree.cols({|cols|
			this.assertEquals(cols,8,"KDTree correct dims after fit")
		});
		Server.default.sync;

		//Test cmd-period resistance
		waitForCounts.test = false;
		Server.default.freeAll;
		Server.default.sync;
		Server.default.sync;
		waitForCounts.wait;

		this.assertEquals(Server.default.numSynths,3,"Dataset: Three Synths remain after cmd-.");
		foo.size({|size|
			this.assertEquals(size,1,"Dataset size is still 1 after Cmd-.");
		});
		Server.default.sync;
		foo.cols({|cols|
			this.assertEquals(cols,8,"Dataset cols is still 8 after Cmd-.");
		});
		Server.default.sync;
		tree.cols({|cols| this.assertEquals(cols,8,"KDTree correct dims after Cmd-.")});
		Server.default.sync;
	}
}