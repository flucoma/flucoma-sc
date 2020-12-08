FluidUMAP : FluidModelObject {

    var <>numDimensions, <>numNeighbours, <>minDist, <>iterations, <>learnRate, <>batchSize;

	*new {|server,numDimensions = 2, numNeighbours = 15, minDist = 0.1, iterations = 200, learnRate = 0.1, batchSize = 50|
		^super.new(server,[numDimensions, numNeighbours, minDist, iterations, learnRate, batchSize])
        .numDimensions_(numDimensions)
        .numNeighbours_(numNeighbours)
        .minDist_(minDist)
        .iterations_(iterations)
        .learnRate_(learnRate)
        .batchSize_(batchSize);
	}

    prGetParams{
        ^[
            this.numDimensions,
            this.numNeighbours,
            this.minDist,
            this.iterations,
            this.learnRate,
            this.batchSize
        ]
    }

    fitTransformMsg{|sourceDataSet, destDataSet|
        ^this.prMakeMsg(\fitTransform, id, sourceDataSet.id, destDataSet.id)
    }

	fitTransform{|sourceDataSet, destDataSet, action|
        actions[\fitTransform] = [nil, action];
		this.prSendMsg(this.fitTransformMsg(sourceDataSet,destDataSet));
	}

	// not implemented
	cols {|action|}
	read{|filename,action|}
	write{|filename,action|}
	size { |action|}

}
