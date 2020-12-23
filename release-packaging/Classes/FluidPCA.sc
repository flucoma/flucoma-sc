FluidPCA : FluidRealTimeModel{

    var <>numDimensions;

    *new {|server, numDimensions = 2|
    	^super.new(server,[numDimensions]).numDimensions_(numDimensions);
    }

    prGetParams{
        ^[numDimensions,-1,-1];
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

        ^FluidProxyUgen.kr(this.class.name.asString++'/query', K2A.ar(trig),
                id, this.numDimensions, this.prEncodeBuffer(inputBuffer), this.prEncodeBuffer(outputBuffer));
    }

}
