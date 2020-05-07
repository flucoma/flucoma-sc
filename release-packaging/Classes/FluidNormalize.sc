FluidNormalize : FluidManipulationClient {

  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

    fit{|dataset, action|
        this.prSendMsg(\fit,[dataset.asString],action);
    }

    normalize{|sourceDataset, destDataset, action|
        this.prSendMsg(\normalize,[sourceDataset.asString, destDataset.asString],action);
    }

    normalizePoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\normalizePoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|
        this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }

}   
