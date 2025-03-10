FluidLSTMRegressor : FluidModelObject {
	var <>hiddenLayers, <>maxIter, <>learnRate, <>momentum, <>batchSize, <>validation;

	*new {|server, hiddenLayers = #[10] , maxIter = 5, learnRate = 0.01, momentum = 0.9, batchSize = 50, validation = 0.2|

		^super.new(server, [hiddenLayers.size] ++ hiddenLayers ++ [maxIter, learnRate, momentum, batchSize, validation])
		.hiddenLayers_(hiddenLayers)
		.maxIter_(maxIter)
		.learnRate_(learnRate)
		.momentum_(momentum)
		.batchSize_(batchSize)
		.validation_(validation);
	}

	prGetParams{
		^[this.id, this.hiddenLayers.size] ++ this.hiddenLayers ++ [this.maxIter, this.learnRate, this.momentum, this.batchSize, this.validation]
	}

	clearMsg{ ^this.prMakeMsg(\clear, id) }

	clear{ |action|
		actions[\clear] = [nil, action];
		this.prSendMsg(this.clearMsg);
	}

	fitMsg{|sourceDataSeries, targetDataSet|
		^this.prMakeMsg(\fit,id,sourceDataSeries.id, targetDataSet.id);
	}

	fit{|sourceDataSeries, targetDataSet, action|
		actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitMsg(sourceDataSeries,targetDataSet));
	}

	predictMsg{|sourceDataSeries, targetDataSet|
		^this.prMakeMsg(\predict,id,sourceDataSeries.id, targetDataSet.id);
	}

	predict{|sourceDataSeries, targetDataSet, action|
		actions[\predict] = [nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSeries,targetDataSet));
	}

	predictSeriesMsg { |sourceBuffer, targetBuffer|
		^this.prMakeMsg(\predictSeries,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			["/b_query", targetBuffer.asUGenInput]);
	}

	predictSeries { |sourceBuffer, targetBuffer, action|
		actions[\predictSeries] = [nil,{action.value(targetBuffer)}];
		this.prSendMsg(this.predictSeriesMsg(sourceBuffer, targetBuffer));
	}

	read { |filename, action|
		actions[\read] = [numbers(FluidMessageResponse,_,nil,_), {
			|data|
			this.prUpdateParams(data);
			action.value;
		}];
		this.prSendMsg(this.readMsg(filename));
	}

	// kr{|trig, inputBuffer,outputBuffer, tapIn = 0, tapOut = -1|
	// 	var params;
	// 	tapIn = tapIn ? this.tapIn;
	// 	tapOut = tapOut ? this.tapOut;
	//
	// 	this.tapIn_(tapIn).tapOut_(tapOut);
	//
	// 	params = [this.prEncodeBuffer(inputBuffer),
	// 	this.prEncodeBuffer(outputBuffer),this.tapIn,this.tapOut];
	//
	// 	^FluidLSTMRegressorQuery.kr(trig,this, *params);
	// }

	prUpdateParams{|data|
		var rest = data.keep(-5);
		this.hiddenLayers_(data.drop(1).drop(-5).copy);
		[\maxIter_, \learnRate_, \momentum_, \batchSize_, \validation_]
		.do{|prop,i|
			this.performList(prop,rest[i]);
		};
	}
}

// FluidLSTMRegressorQuery : FluidRTMultiOutUGen {
// 	*kr{ |trig, model, inputBuffer,outputBuffer, tapIn = 0, tapOut = -1|
// 		^this.multiNew('control',trig, model.asUGenInput,
// 			tapIn, tapOut,
// 		inputBuffer.asUGenInput, outputBuffer.asUGenInput)
// 	}
//
// 	init { arg ... theInputs;
// 		inputs = theInputs;
// 		^this.initOutputs(1, rate);
// 	}
// }

FluidLSTMClassifier : FluidModelObject {
	var <>hiddenLayers, <>maxIter, <>learnRate, <>momentum, <>batchSize, <>validation;

	*new {|server, hiddenLayers = #[10], maxIter = 5, learnRate = 0.01, momentum = 0.9, batchSize = 50, validation = 0.2|
		^super.new(server,[hiddenLayers.size] ++ hiddenLayers ++ [maxIter, learnRate,  momentum, batchSize, validation])
		.hiddenLayers_(hiddenLayers)
		.maxIter_(maxIter)
		.learnRate_(learnRate)
		.momentum_(momentum)
		.batchSize_(batchSize)
		.validation_(validation);
	}

	prGetParams{
		^[this.id, this.hiddenLayers.size] ++ this.hiddenLayers ++ [this.maxIter, this.learnRate,  this.momentum, this.batchSize, this.validation];
	}

	clearMsg{ ^this.prMakeMsg(\clear,id) }

	clear{ |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	fitMsg{|sourceDataSeries, targetLabelSet|
		^this.prMakeMsg(\fit,id,sourceDataSeries.id, targetLabelSet.id);
	}

	fit{|sourceDataSeries, targetLabelSet, action|
		actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitMsg(sourceDataSeries,targetLabelSet));
	}

	predictMsg{|sourceDataSeries, targetLabelSet|
		^this.prMakeMsg(\predict,id,sourceDataSeries.id, targetLabelSet.id);
	}

	predict{ |sourceDataSeries, targetLabelSet, action|
		actions[\predict]=[nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSeries,targetLabelSet));
	}

	predictSeriesMsg { |sourceBuffer|
		^this.prMakeMsg(\predictSeries,id,this.prEncodeBuffer(sourceBuffer))
	}

	predictSeries { |sourceBuffer, action|
		actions[\predictSeries] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictSeriesMsg(sourceBuffer));
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
		var rest = data.keep(-5);
		this.hiddenLayers_(data.drop(1).drop(-5).copy);
		[\maxIter_, \learnRate_, \momentum_, \batchSize_, \validation_]
		.do{|prop,i|
			this.performList(prop,rest[i]);
		};
	}

	// kr{|trig, inputBuffer,outputBuffer|
	//
	// 	var params = [this.prEncodeBuffer(inputBuffer),
	// 	this.prEncodeBuffer(outputBuffer)];
	//
	// 	^FluidLSTMClassifierQuery.kr(trig,this, *params);
	// }
}

// FluidLSTMClassifierQuery : FluidRTMultiOutUGen {
// 	*kr{ |trig, model, inputBuffer,outputBuffer|
// 		^this.multiNew('control',trig, model.asUGenInput,
// 		inputBuffer.asUGenInput, outputBuffer.asUGenInput)
// 	}
//
// 	init { arg ... theInputs;
// 		inputs = theInputs;
// 		^this.initOutputs(1, rate);
// 	}
// }

FluidLSTMForecaster : FluidModelObject {
	var <>hiddenLayers, <>maxIter, <>learnRate, <>momentum, <>batchSize, <>validation, <>forecastLength;

	*new {|server, hiddenLayers = #[10], maxIter = 5, learnRate = 0.01, momentum = 0.9, batchSize = 50, validation = 0.2, forecastLength = 0|
		^super.new(server,[hiddenLayers.size] ++ hiddenLayers ++ [maxIter, learnRate,  momentum, batchSize, validation, forecastLength])
		.hiddenLayers_(hiddenLayers)
		.maxIter_(maxIter)
		.learnRate_(learnRate)
		.momentum_(momentum)
		.batchSize_(batchSize)
		.validation_(validation)
		.forecastLength_(forecastLength);
	}

	prGetParams{
		^[this.id, this.hiddenLayers.size] ++ this.hiddenLayers ++ [this.maxIter, this.learnRate,  this.momentum, this.batchSize, this.validation, this.forecastLength];
	}

	clearMsg{ ^this.prMakeMsg(\clear,id) }

	clear{ |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	fitMsg{|sourceDataSeries|
		^this.prMakeMsg(\fit,id,sourceDataSeries.id);
	}

	fit{|sourceDataSeries, action|
		actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitMsg(sourceDataSeries));
	}

	predictMsg{|sourceDataSeries, targetDataSeries, forecastLength|
		^this.prMakeMsg(\predict,id,sourceDataSeries.id, targetDataSeries.id, forecastLength);
	}

	predict{ |sourceDataSeries, targetDataSeries, forecastLength, action|
		actions[\predict]=[nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSeries, targetDataSeries, forecastLength ? this.forecastLength));
	}

	predictSeriesMsg { |sourceBuffer, targetBuffer, forecastLength|
		^this.prMakeMsg(\predictSeries,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			forecastLength,
			["/b_query", targetBuffer.asUGenInput]);
	}

	predictSeries { |sourceBuffer, targetBuffer, forecastLength, action|
		actions[\predictSeries] = [nil,{action.value(targetBuffer)}];
		this.prSendMsg(this.predictSeriesMsg(sourceBuffer, targetBuffer, forecastLength ? this.forecastLength));
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
		var rest = data.keep(-5);
		this.hiddenLayers_(data.drop(1).drop(-5).copy);
		[\maxIter_, \learnRate_, \momentum_, \batchSize_, \validation_]
		.do{|prop,i|
			this.performList(prop,rest[i]);
		};
	}

	// kr{|trig, inputBuffer,outputBuffer|
	//
	// 	var params = [this.prEncodeBuffer(inputBuffer),
	// 	this.prEncodeBuffer(outputBuffer)];
	//
	// 	^FluidLSTMClassifierQuery.kr(trig,this, *params);
	// }
}

// FluidLSTMForecasterQuery : FluidRTMultiOutUGen {
// 	*kr{ |trig, model, inputBuffer,outputBuffer|
// 		^this.multiNew('control',trig, model.asUGenInput,
// 		inputBuffer.asUGenInput, outputBuffer.asUGenInput)
// 	}
//
// 	init { arg ... theInputs;
// 		inputs = theInputs;
// 		^this.initOutputs(1, rate);
// 	}
// }