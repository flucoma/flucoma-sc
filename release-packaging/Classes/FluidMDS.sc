FluidMDS : FluidManipulationClient {
	classvar < manhattan = 0;
	classvar < euclidean = 1;
	classvar < sqeuclidean = 2;
	classvar < max = 3;
	classvar < min = 4;
	classvar < kl = 5;
	classvar < cosine = 5;

	*new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

	fitTransform{|sourceDataset, destDataset, k, dist, action|
		this.prSendMsg(\fitTransform,[sourceDataset.asSymbol,  destDataset.asSymbol, k, dist],action);
	}

}
