FluidMLPRegressor : FluidManipulationClient {

  classvar <identity = 0;
  classvar <sigmoid  =  1;
  classvar <relu = 2;
  classvar <tanh = 3;

	*new {|server, hidden = #[3,3] , activation = 0, maxIter = 100, learnRate = 0.0001, momentum = 0.9, batchSize = 50|
		var uid = UniqueID.next;
		^super.new(server,*([hidden.size]++hidden++activation++maxIter++learnRate++
			momentum++batchSize++uid))!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

	fit{|sourceDataset, targetDataset, action|
	   this.prSendMsg(\fit,
			[sourceDataset.asSymbol, targetDataset.asSymbol],
			action,numbers(FluidMessageResponse,_,1,_)
		);
	}

	predict{ |sourceDataset, targetDataset, layer, action|
		this.prSendMsg(\predict,
			[sourceDataset.asSymbol, targetDataset.asSymbol,layer],
			action);
	}

	predictPoint { |sourceBuffer, targetBuffer, layer action|
		this.prSendMsg(\predictPoint,
      [sourceBuffer.asUGenInput, targetBuffer.asUGenInput, layer], action);
	}
}
