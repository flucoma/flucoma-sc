FluidPCA : FluidModelObject{

	var <>numDimensions, <>whiten;

	*new {|server, numDimensions = 2, whiten = 0|
		^super.new(server,[numDimensions, whiten]).numDimensions_(numDimensions).whiten_(whiten);
	}

	prGetParams{
		^[this.id, numDimensions, whiten];
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
		actions[\transform] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.transformMsg(sourceDataSet,destDataSet));
	}

	fitTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\fitTransform,id, sourceDataSet.id, destDataSet.id);
	}

	fitTransform{|sourceDataSet, destDataSet, action|
		actions[\fitTransform] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.fitTransformMsg(sourceDataSet,destDataSet));
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

	kr{|trig, inputBuffer,outputBuffer,numDimensions|

		numDimensions = numDimensions ? this.numDimensions;
		this.numDimensions_(numDimensions);

		^FluidPCAQuery.kr(trig ,this, this.prEncodeBuffer(inputBuffer), this.prEncodeBuffer(outputBuffer), this.numDimensions, this.whiten);
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
		this.prSendMsg(this.inverseTransformPointMsg(sourceBuffer,destBuffer));
	}

	inverseTransformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\inverseTransform,id,sourceDataSet.id, destDataSet.id);
	}

	inverseTransform{|sourceDataSet, destDataSet,action|
		actions[\inverseTransform] = [nil,action];
		this.prSendMsg(this.inverseTransformMsg(sourceDataSet, destDataSet));
	}


}

FluidPCAQuery :  FluidRTMultiOutUGen {
	*kr{ |trig, model, inputBuffer,outputBuffer,numDimensions, whiten|
		^this.multiNew('control',trig, model.asUGenInput,
			numDimensions, whiten,
			inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
