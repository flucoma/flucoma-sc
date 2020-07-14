FluidKNNClassifier : FluidDataClient {

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new1(server,[\numNeighbours,numNeighbours,\weight,weight]);
	}

	fit{|dataset, labelset, action|
	   this.prSendMsg(\fit,[dataset.asSymbol, labelset.asSymbol], action);
	}

	predict{|dataset, labelset, action|
		this.prSendMsg(\predict,
			[dataset.asSymbol, labelset.asSymbol],
			action);
	}

	predictPoint {|buffer, action|
		this.prSendMsg(\predictPoint,
			[buffer.asUGenInput], action,
			[string(FluidMessageResponse,_,_)]
		);
	}
}
