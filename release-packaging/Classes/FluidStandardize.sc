FluidStandardize : FluidManipulationClient {

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

    standardize{|sourceDataset, destDataset, action|
        this.prSendMsg(\standardize,[sourceDataset.asString, destDataset.asString],action);
    }

    standardizePoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\standardizePoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
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
