FluidKDTree : FluidManipulationClient {

	var id;

	*new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid).init(uid);
	}

	init {|uid|
		id = uid;
	}

  fit{|dataset,action|
     this.prSendMsg(\fit,[dataset.asUGenInput],action);
  }

  kNearest{ |buffer, k,action|
      this.prSendMsg(\kNearest,[buffer.asUGenInput,k],action,k.collect{string(FluidMessageResponse,_,_)});
  }

  kNearestDist { |buffer, k,action|
      this.prSendMsg(\kNearestDist,[buffer.asUGenInput,k],action,[numbers(FluidMessageResponse,_,k,_)]);
  }

  cols { |action|
      this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
  }

  read{ |filename,action|
      this.prSendMsg(\read,[filename.asString],action);
  }

  write{ |filename,action|
      this.prSendMsg(\write,[filename.asString],action);
  }

	free { |action|
		if(server.serverRunning){this.prSendMsg(\free,[],action)};
		super.free;
	}
}
