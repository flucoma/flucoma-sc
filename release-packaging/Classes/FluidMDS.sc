FluidMDS : FluidManipulationClient {
	classvar < manhattan = 0;
	classvar < euclidean = 1;
	classvar < sqeuclidean = 2;
	classvar < max = 3;
	classvar < min = 4;
	classvar < kl = 5;
	classvar < cosine = 5;

	*new {|server,numDimensions = 2, distanceMetric = 1|
		var uid = UniqueID.next;
		^super.new(server,uid,*[
			\numDimensions,numDimensions,
			\distanceMetric, distanceMetric
		])!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
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
