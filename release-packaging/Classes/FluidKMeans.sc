FluidKMeans : FluidModelObject {

	var clusters, maxiter;

	*new {|server, numClusters = 4, maxIter = 100|
		^super.new(server,[numClusters,maxIter])
		.numClusters_(numClusters)
		.maxIter_(maxIter);
	}

	numClusters_{|n| clusters = n.asInteger}
	numClusters{ ^clusters }

	maxIter_{|i| maxiter = i.asInteger}
	maxIter{ ^maxiter }

	prGetParams{^[this.id,this.numClusters,this.maxIter];}

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

	fitTransformMsg{|srcDataSet, dstDataSet|
		^this.prMakeMsg(\fitTransform, id, srcDataSet.id, dstDataSet.id)
	}

	fitTransform{|srcDataSet, dstDataSet,action|
		actions[\fitTransform] = [nil,action];
		this.prSendMsg(this.fitTransformMsg(srcDataSet,dstDataSet));
	}

	transformMsg{|srcDataSet, dstDataSet|
		^this.prMakeMsg(\transform, id, srcDataSet.id, dstDataSet.id)
	}

	transform{ |srcDataSet, dstDataSet, action|
		actions[\transform] = [nil,action];
		this.prSendMsg(this.transformMsg(srcDataSet,dstDataSet));
	}

	transformPointMsg{ |sourceBuffer, targetBuffer|
		^this.prMakeMsg(\transformPoint, id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			["/b_query", targetBuffer.asUGenInput]);
	}

	transformPoint { |sourceBuffer, targetBuffer, action|
		actions[\transformPoint] = [nil,{action.value(targetBuffer)}];
		this.prSendMsg(this.transformPointMsg(sourceBuffer, targetBuffer));
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
		^FluidKMeansQuery.kr(trig,
			this,
			this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer));
	}
}

FluidKMeansQuery : FluidRTMultiOutUGen {

	*kr{ |trig, model,inputBuffer, outputBuffer |
		^this.multiNew('control',trig, model.asUGenInput,inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
