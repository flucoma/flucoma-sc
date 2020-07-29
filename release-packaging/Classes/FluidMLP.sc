FluidMLPRegressor : FluidRTDataClient {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

	*new {|server, hidden = #[3,3] , activation = 2, outputActivation = 0, tapIn = 0, tapOut = -1,maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|
		var hiddenCtrlLabels;
		hidden = [hidden.size]++hidden;

		hiddenCtrlLabels = hidden.collect{|x,i| \hidden++i};

		^super.new1(server,
			[hiddenCtrlLabels,hidden].lace ++
			[
			\activation,activation,
			\outputActivation, outputActivation,
			\tapIn, tapIn,
			\tapOut, tapOut,
			\maxIter, maxIter,
			\learnRate,learnRate,
			\momentum, momentum,
			\batchsize,batchSize,
			\validation,validation,
		])
	}

	clear{ |action|
		this.prSendMsg(\clear,action:action);
	}

	fit{|sourceDataSet, targetDataSet, action|
	   this.prSendMsg(\fit,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol],
			action,numbers(FluidMessageResponse,_,1,_)
		);
	}

	predict{ |sourceDataSet, targetDataSet, action|
		this.prSendMsg(\predict,
			[sourceDataSet.asSymbol, targetDataSet.asSymbol],
			action);
	}

	predictPoint { |sourceBuffer, targetBuffer, action|
		this.prSendMsg(\predictPoint,
      [sourceBuffer.asUGenInput, targetBuffer.asUGenInput], action);
	}
}


FluidMLPClassifier : FluidRTDataClient {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

	*new {|server, hidden = #[3,3] , activation = 0, maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|
		var hiddenCtrlLabels;
		hidden = [hidden.size]++hidden;

		hiddenCtrlLabels = hidden.collect{|x,i| \hidden++i};

		^super.new1(server,
			[hiddenCtrlLabels,hidden].lace ++
			[
			\activation,activation,
			\maxIter, maxIter,
			\learnRate,learnRate,
			\momentum, momentum,
			\batchsize,batchSize,
			\validation,validation,
		])
	}

	clear{ |action|
		this.prSendMsg(\clear,action:action);
	}


	fit{|sourceDataSet, targetLabelSet, action|
	   this.prSendMsg(\fit,
			[sourceDataSet.asSymbol, targetLabelSet.asSymbol],
			action,numbers(FluidMessageResponse,_,1,_)
		);
	}

	predict{ |sourceDataSet, targetLabelSet, action|
		this.prSendMsg(\predict,
			[sourceDataSet.asSymbol, targetLabelSet.asSymbol],
			action);
	}

	predictPoint { |sourceBuffer, action|
		this.prSendMsg(\predictPoint,
			[sourceBuffer.asUGenInput], action, string(FluidMessageResponse,_,_));
	}
}
