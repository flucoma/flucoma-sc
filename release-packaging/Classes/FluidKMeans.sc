FluidKMeans : FluidManipulationClient {

    var <>k;

    *new {|server|
  		var uid = UniqueID.next;
  		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
  	}

  	init {|uid|
  		id = uid;
  	}

    fit{|dataset,k, maxIter = 100, buffer, action|
       buffer = buffer ? -1;
        this.k = k;
        this.prSendMsg(\fit,[dataset.asSymbol, k,maxIter, buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    fitPredict{|dataset,labelset, k, maxIter = 100, action|
        this.k = k;
		this.prSendMsg(\fitPredict,[dataset.asSymbol,labelset.asSymbol,  k,maxIter],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    predict{ |dataset, labelset,action|
        this.prSendMsg(\predict,[dataset.asSymbol, labelset.asSymbol],action,[numbers(FluidMessageResponse,_,this.k,_)]);
    }

    predictPoint { |buffer, action|
        this.prSendMsg(\predictPoint,[buffer.asUGenInput],action,[number(FluidMessageResponse,_,_)]);
    }

    cols { |action|
		action ?? action = postit;
        this.prSendMsg(\cols,[],action,[number(FluidMessageResponse,_,_)]);
    }

    read{ |filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{ |filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }
}
