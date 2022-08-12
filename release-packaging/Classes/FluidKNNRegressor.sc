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

	predict{ |sourceDataSet, targetDataSet,action|
		actions[\predict] = [nil, action];
		this.prSendMsg(this.predictMsg(sourceDataSet, targetDataSet));
	}

	predictPointMsg { |buffer|
		^this.prMakeMsg(\predictPoint,id, this.prEncodeBuffer(buffer));
	}

	predictPoint { |buffer, action|
		actions[\predictPoint] = [number(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(buffer));
	}

	kr{|trig, inputBuffer,outputBuffer|
		^FluidKNNRegressorQuery.kr(K2A.ar(trig),
			this, this.numNeighbours, this.weight,
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