FluidNormalize : FluidDataClient {

	*new {|server, min = 0, max = 1|
		^super.new1(server,[\min,min,\max,max]);
	}

	fit{|dataSet, action|
		this.prSendMsg(\fit,[dataSet.asSymbol], action);
	}

	transform{|sourceDataSet, destDataSet, action|
		this.prSendMsg(\transform,
			[sourceDataSet.asSymbol, destDataSet.asSymbol], action
		);
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		this.prSendMsg(\fitTransform,
			[sourceDataSet.asSymbol, destDataSet.asSymbol], action
		);
	}

	transformPoint{|sourceBuffer, destBuffer, action|
		this.prSendMsg(\transformPoint,
			[sourceBuffer.asUGenInput, destBuffer.asUGenInput], action
		);
	}
}
