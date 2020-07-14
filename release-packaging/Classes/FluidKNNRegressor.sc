FluidKNNRegressor : FluidDataClient {

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new1(server,[\numNeighbours,numNeighbours,\weight,weight]);
	}

	fit{|sourceDataset, targetDataset, action|
	   this.prSendMsg(\fit,
			[sourceDataset.asSymbol, targetDataset.asSymbol],
			action
		);
	}

	predict{ |sourceDataset, targetDataset,action|
		this.prSendMsg(\predict,
			[sourceDataset.asSymbol, targetDataset.asSymbol],
			action);
	}

	predictPoint { |buffer, action|
		this.prSendMsg(\predictPoint, [buffer.asUGenInput], action,
			[number(FluidMessageResponse,_,_)]);
	}
}
