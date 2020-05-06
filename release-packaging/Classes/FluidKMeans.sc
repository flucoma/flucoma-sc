FluidKMeans : FluidManipulationClient {

    var <>k;
    
    *new {|server|
  		var uid = UniqueID.next;
  		^super.new(server,uid).init(uid);
  	}

  	init {|uid|
  		id = uid;
  	}
    
    fit{|dataset,k, maxIter = 100, buffer, action|
       buffer = buffer ? -1;
        this.k = k;
        this.prSendMsg(\fit,[dataset.asString, k,maxIter, buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    predict{ |dataset, labelset,action|
        this.prSendMsg(\predict,[dataset.asString, labelset.asString],action,[numbers(FluidMessageResponse,_,this.k,_)]);
    }

    getClusters{ |dataset, labelset,action|
        this.prSendMsg(\getClusters,[dataset.asString, labelset.asString],action);
    }

    predictPoint { |buffer, action|
        this.prSendMsg(\predictPoint,[buffer.asUGenInput],action,[number(FluidMessageResponse,_,_)]);
    }

    cols { |action|
        this.prSendMsg(\cols,[],action,[number(FluidMessageResponse,_,_)]);
    }

    read{ |filename,action|
        this.prSendMsg(\read,[filename.asString],action);
    }

    write{ |filename,action|
        this.prSendMsg(\write,[filename.asString],action);
    }
}
