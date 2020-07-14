FluidNormalize : FluidDataClient {

	*new {|server, min = 0, max = 1|
		^super.new1(server,[\min,min,\max,max]);
	}

	fit{|dataset, action|
		this.prSendMsg(\fit,[dataset.asSymbol], action);
	}

	transform{|sourceDataset, destDataset, action|
		this.prSendMsg(\transform,
			[sourceDataset.asSymbol, destDataset.asSymbol], action
		);
	}

	fitTransform{|sourceDataset, destDataset, action|
		this.prSendMsg(\fitTransform,
			[sourceDataset.asSymbol, destDataset.asSymbol], action
		);
	}

	transformPoint{|sourceBuffer, destBuffer, action|
		this.prSendMsg(\transformPoint,
			[sourceBuffer.asUGenInput, destBuffer.asUGenInput], action
		);
	}
}
