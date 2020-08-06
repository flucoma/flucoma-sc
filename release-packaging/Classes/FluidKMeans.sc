FluidKMeans : FluidRTDataClient {

	*new {|server, numClusters = 4, maxIter = 100|
		^super.new1(server,[\numClusters,numClusters,\maxIter,maxIter]);
	}

	fit{|dataSet,action|
		this.prSendMsg(\fit,
			[dataSet.asSymbol], action,
			[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	fitPredict{|dataSet, labelSet,action|
		this.prSendMsg(\fitPredict,
			[dataSet.asSymbol,labelSet.asSymbol],
			action,[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	predict{ |dataSet, labelSet,action|
		this.prSendMsg(\predict,
			[dataSet.asSymbol, labelSet.asSymbol], action,
			[numbers(FluidMessageResponse,_,this.numClusters,_)]
		);
	}

	predictPoint { |buffer, action|
		buffer = this.prEncodeBuffer(buffer);
		this.prSendMsg(\predictPoint,
			[buffer], action,
			[number(FluidMessageResponse,_,_)]
		);
	}
}
