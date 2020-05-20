FluidPCA : FluidManipulationClient {


  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

    fit{|dataset, k, action|
        this.prSendMsg(\fit,[dataset.asSymbol, k],action);
    }

    transform{|sourceDataset, destDataset, action|
        this.prSendMsg(\transform,[sourceDataset.asSymbol, destDataset.asSymbol],action);
    }

    fitTransform{|sourceDataset, destDataset, k, action|
        this.prSendMsg(\fitTransform,[sourceDataset.asSymbol, destDataset.asSymbol, k],action);
    }


    transformPoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\transformPoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|

		action ?? {action = postit};

        this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.prSendMsg(\read,[filename],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename],action);
    }

}
