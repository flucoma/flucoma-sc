FluidKNN : FluidManipulationClient {

  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid).init(uid);
	}

	init {|uid|
		id = uid;
	}

  fit{|dataset, action|
     this.prSendMsg(\fit,[dataset.asString],action);
  }

  classifyPoint{ |buffer, labelset, k, action|
      this.prSendMsg(\classify,[buffer.asUGenInput, labelset.asString, k],action,[string(FluidMessageResponse,_,_)]);
  }

  regressPoint { |buffer,dataset, k, action|
      this.prSendMsg(\regress,[buffer.asUGenInput, dataset.asString,k],action,[number(FluidMessageResponse,_,_)]);
  }
}
