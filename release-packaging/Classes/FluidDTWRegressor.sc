FluidDTWRegressor : FluidModelObject {

	var <>numNeighbours, <>constraint, <>constraintParam;

	*new {|server, numNeighbours = 3, constraint = 0, constraintParam = 0|
		^super.new(server,[numNeighbours, constraint, constraintParam])
		.numNeighbours_(numNeighbours)
		.constraint_(constraint)
		.constraintParam_(constraintParam);
	}

	prGetParams{^[this.id,this.numNeighbours,this.constraint,this.constraintParam];}

	fitMsg{|dataSeries, dataSet|
		^this.prMakeMsg(\fit,this.id,dataSeries.id,dataSet.id)
	}

	fit{|dataSeries, dataSet, action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSeries, dataSet));
	}

	predictMsg{ |dataSeries, dataSet|
		^this.prMakeMsg(\predict,this.id,dataSeries.id,dataSet.id)
	}

	predict{ |dataSeries, dataSet, action|
		actions[\predict] = [nil, action];
		this.prSendMsg(this.predictMsg(dataSeries, dataSet));
	}

	predictSeriesMsg { |sourceBuffer, targetBuffer|
		^this.prMakeMsg(\predictSeries,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(targetBuffer),
			["/b_query", targetBuffer.asUGenInput]);
	}

	predictSeries { |sourceBuffer, targetBuffer, action|
		actions[\predictSeries] = [nil,{action.value(targetBuffer)}];
		this.predictSeriesMsg(sourceBuffer, targetBuffer);
		this.prSendMsg(this.predictSeriesMsg(sourceBuffer, targetBuffer));
	}

	// kr{|trig, inputBuffer, outputBuffer, numNeighbours|
	// 	^FluidKNNRegressorQuery.kr(K2A.ar(trig),
	// 		this, numNeighbours??{this.numNeighbours},
	// 		this.prEncodeBuffer(inputBuffer),
	// 	this.prEncodeBuffer(outputBuffer));
	// }
}

// FluidDTWRegressorQuery : FluidRTMultiOutUGen {
//
// 	*kr{ |trig, model,numNeighbours = 3,inputBuffer, outputBuffer |
// 		^this.multiNew('control',trig, model.asUGenInput,
// 			numNeighbours,
// 		inputBuffer.asUGenInput, outputBuffer.asUGenInput)
// 	}
//
// 	init { arg ... theInputs;
// 		inputs = theInputs;
// 		^this.initOutputs(1, rate);
// 	}
// }