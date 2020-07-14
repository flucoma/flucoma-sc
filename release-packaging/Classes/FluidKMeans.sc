FluidKMeans : FluidDataClient {

	*new {|server, numClusters = 4, maxIter = 100|
		^super.new1(server,[\numClusters,numClusters,\maxIter,maxIter]); 
	}

	fit{|dataset,action|
		this.prSendMsg(\fit,
			[dataset.asSymbol], action,
			[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	fitPredict{|dataset, labelset,action|
		this.prSendMsg(\fitPredict,
			[dataset.asSymbol,labelset.asSymbol],
			action,[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	predict{ |dataset, labelset,action|
		this.prSendMsg(\predict,
			[dataset.asSymbol, labelset.asSymbol], action,
			[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	predictPoint { |buffer, action|
		this.prSendMsg(\predictPoint,
			[buffer.asUGenInput], action,
			[number(FluidMessageResponse,_,_)]
		);
	}
}
