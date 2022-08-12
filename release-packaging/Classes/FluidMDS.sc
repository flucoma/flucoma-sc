FluidMDS : FluidModelObject {
	classvar < manhattan = 0;
	classvar < euclidean = 1;
	classvar < sqeuclidean = 2;
	classvar < max = 3;
	classvar < min = 4;
	classvar < kl = 5;
	classvar < cosine = 5;

	var <>numDimensions, <>distanceMetric;

	*new {|server,numDimensions = 2, distanceMetric = 1|
		^super.new(server,[numDimensions, distanceMetric])
		.numDimensions_(numDimensions)
		.distanceMetric_(distanceMetric);
	}

	prGetParams{
		^[this.numDimensions, this.distanceMetric];
	}

	fitTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\fitTransform,id, sourceDataSet.id,  destDataSet.id);
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		actions[\fitTransform] = [nil,action];
		this.fitTransformMsg(sourceDataSet,destDataSet);

		this.prSendMsg(this.fitTransformMsg(sourceDataSet,destDataSet));
	}

	// not implemented
	cols {|action|}
	read{|filename,action|}
	write{|filename,action|}
	size { |action|}

}
