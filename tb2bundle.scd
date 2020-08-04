s.reboot

(
~bindtest = FluidDataSet(s,\bindtest);
~point = Buffer.alloc(s,10);
~allpoints = Buffer.alloc(s,10,128);
~write = {
	128.do{ |i|
		(i.asString ++ "/128").postln;
		~point.setn(0,10.collect{|j| (i*10) + j});
		~bindtest.addPoint(i,~point);
		s.sync;
	}
};

~read = {
	128.do{|i|
		(i.asString ++ "/128").postln;
		~bindtest.getPoint(i,~point);
		FluidBufCompose.process(s,~point,numChans:1,destination:~allpoints,destStartChan:i);
		s.sync;
	};
	// s.sync;
	// ~allpoints.getToFloatArray(action:{|a| fork{s.sync; a.postln}});
};
)


OSCFunc({"Bing".postln; },"/fartyBalls").oneShot;

~point.setn(0,10.collect{|j|j})

FluidBufCompose.process(s,~point,numChans:1,destination:~allpoints,destStartChan:0);

~ds = FluidDataSet(s,\versioneasteregg);
s.sendMsg("/u_cmd",~ds.synth.nodeID,0,"version")

//The current slow way
fork { ~write.value };
fork { ~read.value };

//Reset
~bindtest.clear;

//Write in one bundle
s.bind(~write)
//Reading back doesn't work yet
s.dumpOSC(0)
s.bind(~read)
~allpoints.getToFloatArray(action:{|a| a.postln});
//But reading the slow way confirms that the writing worked
fork { ~read.value };


