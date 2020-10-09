FluidUMAP : FluidDataClient {

	*new {|server,numDimensions = 2, numNeighbours = 15, minDist = 0.1, iterations = 200, learnRate = 0.1, batchSize = 50|
		^super.new1(server,[
			\numDimensions,numDimensions,
			\numNeighbours, numNeighbours,
			\minDist, minDist,
			\iterations, iterations,
			\learnRate, learnRate,
			\batchSize, batchSize
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
