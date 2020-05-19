FluidNormalize : FluidManipulationClient {

  *new {|server, min = 0, max = 1|
		var uid = UniqueID.next;
		^super.new(server,uid,min,max)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

    fit{|dataset, action|
        this.prSendMsg(\fit,[dataset.asSymbol],action);
    }

	fitTransform{|dataset, action|
        this.prSendMsg(\fit,[dataset.asSymbol],action);
    }

    transform{|sourceDataset, destDataset, action|
        this.prSendMsg(\transform,[sourceDataset.asSymbol, destDataset.asSymbol],action);
    }

    transformPoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\transformPoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|
		action ?? {action = postit};
        this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }

}
