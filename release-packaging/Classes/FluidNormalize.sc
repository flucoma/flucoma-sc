FluidNormalize : FluidModelObject {

	var <>min, <>max;

	*new {|server, min = 0, max = 1|
		^super.new(server,[min,max])
		.min_(min).max_(max);
	}

	prGetParams{
		^[this.id, this.min,this.max,-1,-1];
	}


	fitMsg{|dataSet|
		^this.prMakeMsg(\fit,id,dataSet.id)
	}

	fit{|dataSet, action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSet));
	}

	transformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\transform,id,sourceDataSet.id,destDataSet.id);
	}

	transform{|sourceDataSet, destDataSet, action|
		actions[\transform] = [nil,action];
		this.prSendMsg(this.transformMsg(sourceDataSet, destDataSet));
	}

	fitTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\fitTransform,id,sourceDataSet.id,destDataSet.id)
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		actions[\fitTransform] = [nil,action];
		this.prSendMsg(this.fitTransformMsg(sourceDataSet, destDataSet));
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
		this.prSendMsg(this.transformPointMsg(sourceBuffer, destBuffer));
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
		actions[\inverseTransformPoint] = [nil,{action.value(destBuffer)}];
		this.prSendMsg(this.inverseTransformPointMsg(sourceBuffer, destBuffer));
	}

	kr{|trig, inputBuffer,outputBuffer,min = 0 ,max = 1,invert = 0|

		min = min ? this.min;
		max = max ? this.max;

		this.min_(min).max_(max);

		^FluidNormalizeQuery.kr(trig,
			this, this.prEncodeBuffer(inputBuffer), this.prEncodeBuffer(outputBuffer), this.min, this.max, invert);
	}


}

FluidNormalizeQuery : FluidRTMultiOutUGen {

	*kr{ |trig, model,inputBuffer,outputBuffer,min = 0 ,max = 1,invert = 0|
		// inputBuffer.asUGenInput.postln;
		^this.multiNew('control',trig, model.asUGenInput,
			min,max,invert,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}