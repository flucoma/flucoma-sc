FluidKNNRegressor : FluidModelObject {

	var <>numNeighbours, <>weight;

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new(server,[numNeighbours,weight])
		.numNeighbours_(numNeighbours)
		.weight_(weight);
	}

	prGetParams{^[this.id,this.numNeighbours,this.weight,-1,-1];}

	fitMsg{|sourceDataSet, targetDataSet|
		^this.prMakeMsg(\fit,this.id,sourceDataSet.id,targetDataSet.id)
	}

	fit{|sourceDataSet, targetDataSet, action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(sourceDataSet, targetDataSet));
	}

	predictMsg{ |sourceDataSet, targetDataSet|
		^this.prMakeMsg(\predict,this.id,sourceDataSet.id,targetDataSet.id)
	}

	predict{ |sourceDataSet, targetDataSet, action|
		actions[\predict] = [nil, action];
		this.prSendMsg(this.predictMsg(sourceDataSet, targetDataSet));
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

	kr{|trig, inputBuffer, outputBuffer, numNeighbours, weight|
		^FluidKNNRegressorQuery.kr(K2A.ar(trig),
			this, numNeighbours??{this.numNeighbours}, weight??{this.weight},
			this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer));
	}
}

FluidKNNRegressorQuery : FluidRTMultiOutUGen {

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