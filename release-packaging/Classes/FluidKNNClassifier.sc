FluidKNNClassifier : FluidDataClient {

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new1(server,[\numNeighbours,numNeighbours,\weight,weight]);
	}

	fit{|dataSet, labelSet, action|
	   this.prSendMsg(\fit,[dataSet.asSymbol, labelSet.asSymbol], action);
	}

	predict{|dataSet, labelSet, action|
		this.prSendMsg(\predict,
			[dataSet.asSymbol, labelSet.asSymbol],
			action);
	}

	predictPoint {|buffer, action|
		this.prSendMsg(\predictPoint,
			[buffer.asUGenInput], action,
			[string(FluidMessageResponse,_,_)]
		);
	}
}
