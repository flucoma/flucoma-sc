FluidUMAP : FluidDataClient {

	*new {|server,numDimensions = 2, numNeighbours = 15, minDist = 0.1, maxIter = 200, learnRate = 0.1|
		^super.new1(server,[
			\numDimensions,numDimensions,
			\numNeighbours, numNeighbours,
			\minDist, minDist,
			\maxIter, maxIter,
			\learnRate, learnRate
		])
	}


	fitTransform{|sourceDataSet, destDataSet, action|
		this.prSendMsg(\fitTransform,
			[sourceDataSet.asSymbol,  destDataSet.asSymbol], action);
	}

	// not implemented
	cols {|action|}
	read{|filename,action|}
	write{|filename,action|}
	size { |action|}

}
