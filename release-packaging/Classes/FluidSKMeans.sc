FluidSKMeans : FluidModelObject {

	var clusters, threshold, maxiter;

	*new {|server, numClusters = 4, encodingThreshold = 0.25, maxIter = 100|
		^super.new(server,[numClusters,maxIter, encodingThreshold])
		.numClusters_(numClusters)
		.encodingThreshold_(encodingThreshold)
		.maxIter_(maxIter);
	}

	numClusters_{|n| clusters = n.asInteger}
	numClusters{ ^clusters }

	encodingThreshold_{|t| threshold = t.asFloat}
	encodingThreshold{ ^threshold }

	maxIter_{|i| maxiter = i.asInteger}
	maxIter{ ^maxiter }


	prGetParams{^[this.id,this.numClusters, this.encodingThreshold, this.maxIter];}

	fitMsg{ |dataSet| ^this.prMakeMsg(\fit,id,dataSet.id);}

	fit{|dataSet, action|
		actions[\fit] = [
			numbers( FluidMessageResponse, _, this.numClusters ,_),
			action
		];
		this.prSendMsg(this.fitMsg(dataSet));
	}

	fitPredictMsg{|dataSet, labelSet|
		^this.prMakeMsg(\fitPredict, id, dataSet.id, labelSet.id)
	}

	fitPredict{|dataSet, labelSet,action|
		actions[\fitPredict] = [
			numbers(FluidMessageResponse, _, this.numClusters, _),
			action
		];
		this.prSendMsg(this.fitPredictMsg(dataSet,labelSet));
	}

	predictMsg{|dataSet, labelSet|
		^this.prMakeMsg(\predict, id, dataSet.id, labelSet.id)
	}

	predict{ |dataSet, labelSet, action|
		actions[\predict] = [
			numbers(FluidMessageResponse, _, this.numClusters, _),
			action
		];
		this.prSendMsg(this.predictMsg(dataSet,labelSet));
	}

	predictPointMsg{|buffer|
		^this.prMakeMsg(\predictPoint, id, this.prEncodeBuffer(buffer))
	}

	predictPoint { |buffer, action|
		actions[\predictPoint] = [number(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(buffer))
	}

	fitEncodeMsg{|srcDataSet, dstDataSet|
		^this.prMakeMsg(\fitEncode, id, srcDataSet.id, dstDataSet.id)
	}

	fitEncode{|srcDataSet, dstDataSet,action|
		actions[\fitEncode] = [nil,action];
		this.prSendMsg(this.fitEncodeMsg(srcDataSet,dstDataSet));
	}

	encodeMsg{|srcDataSet, dstDataSet|
		^this.prMakeMsg(\encode, id, srcDataSet.id, dstDataSet.id)
	}

	encode{ |srcDataSet, dstDataSet, action|
		actions[\encode] = [nil,action];
		this.prSendMsg(this.encodeMsg(srcDataSet,dstDataSet));
	}

	encodePointMsg{ |sourceBuffer, targetBuffer|
		^this.prMakeMsg(\encodePoint, id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			["/b_query", targetBuffer.asUGenInput]);
	}

	encodePoint { |sourceBuffer, targetBuffer, action|
		actions[\encodePoint] = [nil,{action.value(targetBuffer)}];
		this.prSendMsg(this.encodePointMsg(sourceBuffer, targetBuffer));
	}

	getMeansMsg{|dataSet| ^this.prMakeMsg(\getMeans, id, dataSet.asUGenInput) }

	getMeans{ |dataSet, action|
		actions[\getMeans] = [nil, action];
		this.prSendMsg(this.getMeansMsg(dataSet));
	}

	setMeansMsg{|dataSet| ^this.prMakeMsg(\setMeans, id, dataSet.asUGenInput) }

	setMeans{ |dataSet, action|
		actions[\setMeans] = [nil, action];
		this.prSendMsg(this.setMeansMsg(dataSet));
	}

	clearMsg{ ^this.prMakeMsg(\clear, id) }

	clear{ |action|
		actions[\clear] = [nil, action];
		this.prSendMsg(this.clearMsg);
	}

	kr{|trig, inputBuffer,outputBuffer|
		^FluidSKMeansQuery.kr(trig,
			this,
			this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer));
	}
}

FluidSKMeansQuery : FluidRTMultiOutUGen {

	*kr{ |trig, model,inputBuffer, outputBuffer |
		^this.multiNew('control',trig, model.asUGenInput,inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
