FluidStandardize : FluidModelObject {

	*new {|server|
		^super.new(server,[]);
	}

	prGetParams{
		^[this.id];
	}

	fitMsg{|dataSet|
		^this.prMakeMsg(\fit,id,dataSet.id);
	}

	fit{|dataSet, action|
		actions[\fit] = [nil, action];
		this.prSendMsg(this.fitMsg(dataSet));
	}

	transformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\transform,id,sourceDataSet.id,destDataSet.id);
	}

	transform{|sourceDataSet, destDataSet, action|
		actions[\transform] = [nil,action];
		this.prSendMsg(this.transformMsg(sourceDataSet,destDataSet));
	}

	fitTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\fitTransform,id,sourceDataSet.id,destDataSet.id)
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		actions[\fitTransform] = [nil,action];
		this.prSendMsg(this.fitTransformMsg(sourceDataSet, destDataSet));
	}


	transformPointMsg{|sourceBuffer, destBuffer|
		^this.prMakeMsg(\transformPoint, id, this.prEncodeBuffer(sourceBuffer), this.prEncodeBuffer(destBuffer),["/b_query",destBuffer.asUGenInput]);
	}

	transformPoint{|sourceBuffer, destBuffer, action|
		actions[\transformPoint] = [nil, {action.value(destBuffer)}];
		this.prSendMsg(this.transformPointMsg(sourceBuffer,destBuffer));
	}

	inverseTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\inverseTransform,id,sourceDataSet.id,destDataSet.id);
	}

	inverseTransform{|sourceDataSet, destDataSet, action|
		actions[\inverseTransform] = [nil,action];
		this.prSendMsg(this.inverseTransformMsg(sourceDataSet, destDataSet));
	}

	inverseTransformPointMsg{|sourceBuffer, destBuffer|
		^this.prMakeMsg(\inverseTransformPoint,id,
			this.prEncodeBuffer(sourceBuffer),
			this.prEncodeBuffer(destBuffer),
			["/b_query",destBuffer.asUGenInput]
		);
	}

	inverseTransformPoint{|sourceBuffer, destBuffer, action|
		actions[\inverseRransformPoint] = [nil,{action.value(destBuffer)}];
		this.prSendMsg(this.inverseTransformPointMsg(sourceBuffer, destBuffer));
	}

	kr{|trig, inputBuffer,outputBuffer,invert = 0|

		^FluidStandardizeQuery.kr(trig,this, this.prEncodeBuffer(inputBuffer), this.prEncodeBuffer(outputBuffer), invert);
	}
}

FluidStandardizeQuery : FluidRTMultiOutUGen {
	*kr{ |trig, model,inputBuffer,outputBuffer,invert = 0|
		^this.multiNew('control',trig, model.asUGenInput,
			invert,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
