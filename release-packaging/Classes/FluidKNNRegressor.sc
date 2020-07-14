FluidKNNRegressor : FluidDataClient {

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new1(server,[\numNeighbours,numNeighbours,\weight,weight]);
	}

	fit{|sourceDataSet, targetDataSet, action|
	   this.prSendMsg(\fit,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol],
			action
		);
	}

	predict{ |sourceDataSet, targetDataSet,action|
		this.prSendMsg(\predict,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol],
			action);
	}

	predictPoint { |buffer, action|
		this.prSendMsg(\predictPoint, [buffer.asUGenInput], action,
			[number(FluidMessageResponse,_,_)]);
	}
}
