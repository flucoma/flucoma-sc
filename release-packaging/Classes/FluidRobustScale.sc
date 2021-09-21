FluidRobustScale : FluidModelObject {

    var <>low, <>high, <>invert;

	*new {|server, low = 25, high = 75, invert = 0|
		^super.new(server,[low,high,invert])
		.low_(low).high_(high).invert_(invert);
	}

    prGetParams{
        ^[this.id,this.low,this.high,this.invert];
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

    kr{|trig, inputBuffer,outputBuffer,invert|

		invert = invert ? this.invert;

        // this.invert_(invert);

        ^FluidRobustScaleQuery.kr(trig,this, this.prEncodeBuffer(inputBuffer), this.prEncodeBuffer(outputBuffer), invert,);
    }


}

FluidRobustScaleQuery : FluidRTMultiOutUGen {
       *kr{ |trig, model, inputBuffer,outputBuffer,invert|
        ^this.multiNew('control',trig, model.asUGenInput,
            invert,
            inputBuffer.asUGenInput, outputBuffer.asUGenInput)
    }

    init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}
}
