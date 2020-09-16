FluidKDTree : FluidRTDataClient {

	*new {|server,numNeighbours = 1, radius = 0, lookupDataSet = ""|
		var env;
		var names = [\numNeighbours, \radius]
		++ this.prServerString(lookupDataSet.asSymbol).collect{|x,i|
			("lookupDataSet"++i).asSymbol;
		};

		var values  = [numNeighbours, radius] ++ this.prServerString(lookupDataSet.asSymbol);
		var params = [names,values].lace;


		/* env = Environment();
		synthControls[1..].do{|x|
			env.put(x,0);
		};
		env.put(\numNeighbours,1); */

		^super.new1(server,params);
			/* env,
			[\numNeighbours]++lookupDataSet); */
	}

	fit{|dataSet,action|
		this.prSendMsg(\fit, [dataSet.asSymbol], action);
	}

	kNearest{ |buffer, action|
		this.prSendMsg(\kNearest,
			[buffer.asUGenInput], action,
			this.numNeighbours.collect{string(FluidMessageResponse,_,_)}
		);
	}

	kNearestDist { |buffer, action|
		this.prSendMsg(\kNearestDist, [buffer.asUGenInput], action,
			[numbers(FluidMessageResponse,_,this.numNeighbours,_)]
		);
	}
}
