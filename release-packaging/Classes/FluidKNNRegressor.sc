FluidKNNRegressor : FluidManipulationClient {

	*new {|server|
        var uid = UniqueID.next;
        ^super.new(server,uid)!?{|inst|inst.init(uid);inst}
    }

    init {|uid|
        id = uid;
    }

    fit{|sourceDataset, targetDataset, action|
       this.prSendMsg(\fit,
			[sourceDataset.asSymbol, targetDataset.asSymbol],
			action
		);
    }

    predict{ |sourceDataset, targetDataset, k, uniform = 0, action|
        this.prSendMsg(\predict,
			[sourceDataset.asSymbol, targetDataset.asSymbol, k, uniform],
			action);
    }

    predictPoint { |buffer, k, uniform = 0, action|
        this.prSendMsg(\predictPoint, [buffer.asUGenInput, k,uniform], action,
			[number(FluidMessageResponse,_,_)]);
    }

	read{|filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }

}
