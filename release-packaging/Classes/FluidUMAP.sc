FluidUMAP : FluidModelObject {

	var <>numDimensions, <>numNeighbours, <>minDist, <>iterations, <>learnRate;

	*new {|server,numDimensions = 2, numNeighbours = 15, minDist = 0.1, iterations = 200, learnRate = 0.1|
		^super.new(server,[numDimensions, numNeighbours, minDist, iterations, learnRate])
		.numDimensions_(numDimensions)
		.numNeighbours_(numNeighbours)
		.minDist_(minDist)
		.iterations_(iterations)
		.learnRate_(learnRate);
	}

	prGetParams{
		^[
			this.id,
			this.numDimensions,
			this.numNeighbours,
			this.minDist,
			this.iterations,
			this.learnRate,
		]
	}

	fitTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\fitTransform, id, sourceDataSet.id, destDataSet.id)
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		actions[\fitTransform] = [nil, action];
		this.prSendMsg(this.fitTransformMsg(sourceDataSet,destDataSet));
	}

	fitMsg{|dataSet|
		^this.prMakeMsg(\fit,id, dataSet.id);
	}

	fit{|dataSet, action|
		actions[\fit] = [nil, action];
		this.prSendMsg(this.fitMsg(dataSet));
	}

	transformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\transform, id, sourceDataSet.id, destDataSet.id);
	}

	transform{|sourceDataSet, destDataSet, action|
		actions[\transform] = [nil, action];
		this.prSendMsg(this.transformMsg(sourceDataSet,destDataSet));
	}


	transformPointMsg{|sourceBuffer, destBuffer|
		^this.prMakeMsg(\transformPoint,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(destBuffer),
			["/b_query",destBuffer.asUGenInput]
		);
	}

	transformPoint{|sourceBuffer, destBuffer, action|
		actions[\transformPoint] = [nil,{action.value(destBuffer)}];
		this.prSendMsg(this.transformPointMsg(sourceBuffer,destBuffer));
	}

	kr{|trig, inputBuffer,outputBuffer|

		^FluidUMAPQuery.kr(trig,
			this,
			this.prEncodeBuffer(inputBuffer),
			this.prEncodeBuffer(outputBuffer));
	}

	// not implemented
	cols {|action|}
	size { |action|}
}

FluidUMAPQuery : FluidRTMultiOutUGen {
	*kr{ |trig, model, inputBuffer,outputBuffer|
		^this.multiNew('control',trig, model.asUGenInput,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
