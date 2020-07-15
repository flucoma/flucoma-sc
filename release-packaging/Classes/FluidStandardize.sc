FluidStandardize : FluidRTDataClient {
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
		this.prSendMsg(\transformPoint,
			[sourceBuffer.asUGenInput, destBuffer.asUGenInput], action
		);
	}
}
