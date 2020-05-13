FluidMDS : FluidManipulationClient {
	var id;

  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid).init(uid);
	}

	init {|uid|
		id = uid;
	}

    fitTransform{|sourceDataset, k, dist, destDataset, action|
		this.pr_sendMsg(\fitTransform,[sourceDataset.asString, k, dist,  destDataset.asString],action);
    }

}
