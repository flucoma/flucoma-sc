FluidKNNClassifier : FluidModelObject {

	var <>numNeighbours, <>weight;

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new(server,[numNeighbours,weight])
		.numNeighbours_(numNeighbours)
		.weight_(weight);
	}

	prGetParams{^[this.id,this.numNeighbours,this.weight];}

	fitMsg{|dataSet, labelSet|
		^this.prMakeMsg(\fit, id, dataSet.id, labelSet.id)
	}

	fit{|dataSet, labelSet, action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSet, labelSet));
	}

	predictMsg{|dataSet, labelSet|
		^this.prMakeMsg(\predict, id, dataSet.id, labelSet.id)
	}

	predict{|dataSet, labelSet, action|
		actions[\predict] = [nil, action];
		this.prSendMsg(this.predictMsg(dataSet, labelSet));
	}

	predictPointMsg{|buffer|
		^this.prMakeMsg(\predictPoint, id, this.prEncodeBuffer(buffer))
	}

	predictPoint {|buffer, action|
		actions[\predictPoint] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(buffer));
	}

	kr{|trig, inputBuffer,outputBuffer|
		^FluidKNNClassifierQuery.kr(trig,
			this, this.numNeighbours, this.weight,
			this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer));
	}

}

FluidKNNClassifierQuery : FluidRTMultiOutUGen {

	*kr{ |trig, model,numNeighbours = 3, weight = 1,inputBuffer, outputBuffer |
		^this.multiNew('control',trig, model.asUGenInput,
			numNeighbours,weight,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}

