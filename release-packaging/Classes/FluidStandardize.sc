FluidStandardize : FluidRTDataClient {
	*new {|server, invert = 0|
		^super.new1(server,[\invert, invert]);
	}

	fit{|dataSet, action|
		this.prSendMsg(\fit, [dataSet.asSymbol], action);
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
		sourceBuffer = this.prEncodeBuffer(sourceBuffer);
		destBuffer = this.prEncodeBuffer(destBuffer);
		this.prSendMsg(\transformPoint,
			[sourceBuffer, destBuffer], action, outputBuffers:[destBuffer]
		);
	}
}
