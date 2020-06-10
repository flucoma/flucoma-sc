FluidKNNClassifier : FluidManipulationClient {

	*new {|server|
        var uid = UniqueID.next;
        ^super.new(server,uid)!?{|inst|inst.init(uid);inst}
    }

    init {|uid|
        id = uid;
    }

    fit{|dataset, labelset, action|
       this.prSendMsg(\fit,[dataset.asSymbol, labelset.asSymbol], action);
    }

    predict{ |dataset, labelset, k, uniform = 0, action|
        this.prSendMsg(\predict,
			[dataset.asSymbol, labelset.asSymbol, k, uniform],
			action);
    }

    predictPoint { |buffer, k, uniform = 0, action|
        this.prSendMsg(\predictPoint,
			[buffer.asUGenInput, k,uniform], action,
			[string(FluidMessageResponse,_,_)]
		);
	}

	read{|filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }

}