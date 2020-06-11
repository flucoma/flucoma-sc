FluidStandardize : FluidManipulationClient {

  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

    fit{|dataset, action|
        this.prSendMsg(\fit,[dataset.asSymbol],action);
    }

    transform{|sourceDataset, destDataset, action|
        this.prSendMsg(\transform,[sourceDataset.asSymbol, destDataset.asSymbol],action);
    }

	fitTransform{|dataset, action|
        this.prSendMsg(\fitTransform,[dataset.asSymbol],action);
    }

    transformPoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\transformPoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }
}
