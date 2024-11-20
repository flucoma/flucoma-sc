FluidDTWClassifier : FluidModelObject {

	var <>numNeighbours, <>constraint, <>constraintParam;

	*new {|server, numNeighbours = 3, constraint = 0, constraintParam = 0|
		^super.new(server,[numNeighbours, constraint, constraintParam])
		.numNeighbours_(numNeighbours)
		.constraint_(constraint)
		.constraintParam_(constraintParam);
	}

	prGetParams{^[this.id,this.numNeighbours,this.constraint,this.constraintParam];}

	fitMsg{|dataSeries, labelSet|
		^this.prMakeMsg(\fit, id, dataSeries.id, labelSet.id)
	}

	fit{|dataSeries, labelSet, action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSeries, labelSet));
	}

	predictMsg{|dataSeries, labelSet|
		^this.prMakeMsg(\predict, id, dataSeries.id, labelSet.id)
	}

	predict{|dataSeries, labelSet, action|
		actions[\predict] = [nil, action];
		this.prSendMsg(this.predictMsg(dataSeries, labelSet));
	}

	predictSeriesMsg{|buffer|
		^this.prMakeMsg(\predictSeries, id, this.prEncodeBuffer(buffer))
	}

	predictSeries {|buffer, action|
		actions[\predictSeries] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictSeriesMsg(buffer));
	}

	// kr{|trig, inputBuffer,outputBuffer|
	// 	^FluidDTWClassifierQuery.kr(trig,
	// 		this, this.numNeighbours,
	// 		this.prEncodeBuffer(inputBuffer),
	// 	this.prEncodeBuffer(outputBuffer));
	// }

}

// FluidDTWClassifierQuery : FluidRTMultiOutUGen {
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

