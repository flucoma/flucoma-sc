FluidMLPRegressor : FluidManipulationClient {

  const <identity = 0;
  const <sigmoid  =  1;
  const <relu = 2;
  const <tanh = 3;

	*new {|server, hidden = #[3,3] , activation = 0, maxIter = 100, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|
		var uid = UniqueID.next;
		hidden = [hidden.size]++hidden;
		^super.new(server,uid, *[
			\hidden,hidden,
			\activation,activation,
			\maxIter, maxIter,
			\learnRate,learnRate,
			\momentum, momentum,
			\batchsize,batchSize,
			\validation,validation,
		])!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

	fit{|sourceDataSet, targetDataSet, action|
	   this.prSendMsg(\fit,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol],
			action,numbers(FluidMessageResponse,_,1,_)
		);
	}

	predict{ |sourceDataSet, targetDataSet, layer, action|
		this.prSendMsg(\predict,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol,layer],
			action);
	}

	predictPoint { |sourceBuffer, targetBuffer, layer action|
		this.prSendMsg(\predictPoint,
      [sourceBuffer.asUGenInput, targetBuffer.asUGenInput, layer], action);
	}
}
