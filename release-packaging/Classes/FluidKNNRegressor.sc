FluidKNNRegressor : FluidRealTimeModel {

    var <>numNeighbours, <>weight;

	*new {|server, numNeighbours = 3, weight = 1|
		^super.new(server,[numNeighbours,weight])
        .numNeighbours_(numNeighbours)
        .weight_(weight);
	}

    prGetParams{^[this.numNeighbours,this.weight,-1,-1];}

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
        ^FluidProxyUgen.kr(this.class.name.asString++'/query', K2A.ar(trig),
                id, this.numNeighbours, this.weight,
                this.prEncodeBuffer(inputBuffer),
                this.prEncodeBuffer(outputBuffer));
    }
}
