FluidMLPRegressor : FluidModelObject {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

	var <>hiddenLayers, <>activation, <>outputActivation, <>tapIn, <>tapOut, <>maxIter, <>learnRate, <>momentum, <>batchSize, <>validation;

	*new {|server, hiddenLayers = #[3,3] , activation = 2, outputActivation = 0, tapIn = 0, tapOut = -1,maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|

		^super.new(server, [hiddenLayers.size] ++ hiddenLayers ++ [activation, outputActivation, tapIn, tapOut, maxIter, learnRate, momentum, batchSize, validation])
		.hiddenLayers_(hiddenLayers)
		.activation_(activation)
		.outputActivation_(outputActivation)
		.tapIn_(tapIn)
		.tapOut_(tapOut)
		.maxIter_(maxIter)
		.learnRate_(learnRate)
		.momentum_(momentum)
		.batchSize_(batchSize)
		.validation_(validation);
	}

	prGetParams{
		^[this.id, this.hiddenLayers.size] ++ this.hiddenLayers ++ [this.activation, this.outputActivation, this.tapIn, this.tapOut, this.maxIter, this.learnRate, this.momentum, this.batchSize, this.validation]
	}

	clearMsg{ ^this.prMakeMsg(\clear, id) }

	clear{ |action|
		actions[\clear] = [nil, action];
		this.prSendMsg(this.clearMsg);
	}

	fitMsg{|sourceDataSet, targetDataSet|
		^this.prMakeMsg(\fit,id,sourceDataSet.id, targetDataSet.id);
	}

	fit{|sourceDataSet, targetDataSet, action|
		actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitMsg(sourceDataSet,targetDataSet));
	}

	predictMsg{|sourceDataSet, targetDataSet|
		^this.prMakeMsg(\predict,id,sourceDataSet.id, targetDataSet.id);
	}

	predict{|sourceDataSet, targetDataSet, action|
		actions[\predict] = [nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSet,targetDataSet));
	}


	predictPointMsg { |sourceBuffer, targetBuffer|
		^this.prMakeMsg(\predictPoint,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			["/b_query", targetBuffer.asUGenInput]);
	}

	predictPoint { |sourceBuffer, targetBuffer, action|
		actions[\predictPoint] = [nil,{action.value(targetBuffer)}];
		this.predictPointMsg(sourceBuffer, targetBuffer);
		this.prSendMsg(this.predictPointMsg(sourceBuffer, targetBuffer));
	}

	read { |filename, action|
		actions[\read] = [numbers(FluidMessageResponse,_,nil,_), {
			|data|
			this.prUpdateParams(data);
			action.value;
		}];
		this.prSendMsg(this.readMsg(filename));
	}

	kr{|trig, inputBuffer,outputBuffer, tapIn = 0, tapOut = -1|
		var params;
		tapIn = tapIn ? this.tapIn;
		tapOut = tapOut ? this.tapOut;

		this.tapIn_(tapIn).tapOut_(tapOut);

		params = [this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer),this.tapIn,this.tapOut];

		^FluidMLPRegressorQuery.kr(trig,this, *params);
	}

	prUpdateParams{|data|
		var rest = data.keep(-9);
		this.hiddenLayers_(data.drop(1).drop(-9).copy);
		[\activation_, \outputActivation_,
			\tapIn_, \tapOut_, \maxIter_,
			\learnRate_, \momentum_,
			\batchSize_, \validation_]
		.do{|prop,i|
			this.performList(prop,rest[i]);
		};
	}
}

FluidMLPRegressorQuery : FluidRTMultiOutUGen {
	*kr{ |trig, model, inputBuffer,outputBuffer, tapIn = 0, tapOut = -1|
		^this.multiNew('control',trig, model.asUGenInput,
			tapIn, tapOut,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}

FluidMLPClassifier : FluidModelObject {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

	var <>hiddenLayers, <>activation, <> maxIter, <>learnRate, <> momentum, <>batchSize, <>validation;

	*new {|server, hiddenLayers = #[3,3] , activation = 2, maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|
		^super.new(server,[hiddenLayers.size] ++ hiddenLayers ++ [activation,  maxIter, learnRate,  momentum, batchSize, validation])
		.hiddenLayers_(hiddenLayers)
		.activation_(activation)
		.maxIter_(maxIter)
		.learnRate_(learnRate)
		.momentum_(momentum)
		.batchSize_(batchSize)
		.validation_(validation);
	}

	prGetParams{
		^[this.id, this.hiddenLayers.size] ++ this.hiddenLayers ++ [this.activation,  this.maxIter, this.learnRate,  this.momentum, this.batchSize, this.validation];
	}

	clearMsg{ ^this.prMakeMsg(\clear,id) }

	clear{ |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	fitMsg{|sourceDataSet, targetLabelSet|
		^this.prMakeMsg(\fit,id,sourceDataSet.id, targetLabelSet.id);
	}

	fit{|sourceDataSet, targetLabelSet, action|
		actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitMsg(sourceDataSet,targetLabelSet));
	}

	predictMsg{|sourceDataSet, targetLabelSet|
		^this.prMakeMsg(\predict,id,sourceDataSet.id, targetLabelSet.id);
	}

	predict{ |sourceDataSet, targetLabelSet, action|
		actions[\predict]=[nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSet,targetLabelSet));
	}

	predictPointMsg { |sourceBuffer|
		^this.prMakeMsg(\predictPoint,id,this.prEncodeBuffer(sourceBuffer))
	}

	predictPoint { |sourceBuffer, action|
		actions[\predictPoint] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(sourceBuffer));
	}


	read { |filename, action|
		actions[\read] = [numbers(FluidMessageResponse,_,nil,_), {
			|data|
			this.prUpdateParams(data);
			action.value;
		}];
		this.prSendMsg(this.readMsg(filename));
	}

	prUpdateParams{|data|
		var rest = data.keep(-6);
		this.hiddenLayers_(data.drop(1).drop(-6).copy);
		[\activation_, \maxIter_,
			\learnRate_, \momentum_,
			\batchSize_, \validation_]
		.do{|prop,i|
			this.performList(prop,rest[i]);
		};
	}

	kr{|trig, inputBuffer,outputBuffer|

		var params = [this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer)];

		^FluidMLPClassifierQuery.kr(trig,this, *params);
	}
}

FluidMLPClassifierQuery : FluidRTMultiOutUGen {
	*kr{ |trig, model, inputBuffer,outputBuffer|
		^this.multiNew('control',trig, model.asUGenInput,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
