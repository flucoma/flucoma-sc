FluidPCA : FluidManipulationClient {


  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

    fit{|dataset, k, action|
        this.prSendMsg(\fit,[dataset.asString, k],action);
    }

    transform{|sourceDataset, destDataset, action|
        this.prSendMsg(\transform,[sourceDataset.asString, destDataset.asString],action);
    }

    fitTransform{|sourceDataset, k, destDataset, action|
        this.prSendMsg(\fitTransform,[sourceDataset.asString,k,  destDataset.asString],action);
    }


    transformPoint{|sourceBuffer, destBuffer, action|
        this.prSendMsg(\transformPoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|
        this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

	rows {|action|
        this.prSendMsg(\rows,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.prSendMsg(\read,[filename],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename],action);
    }

}   
