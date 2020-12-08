FluidKDTree : FluidRealTimeModel
 {

    var neighbours,radius,lookup;

    *new{ |server, numNeighbours = 1, radius = 0, lookupDataSet|
        ^super.new(server,[numNeighbours,radius,lookupDataSet ? -1])
        .numNeighbours_(numNeighbours)
        .radius_(radius)
        .lookupDataSet_(lookupDataSet);
    }

    numNeighbours_{|k|neighbours = k.asInteger; }
    numNeighbours{ ^neighbours; }

    radius_{|r| radius = r.asUGenInput;}
    radius{ ^radius; }

    lookupDataSet_{|ds| lookup = ds ? -1; }
    lookupDataSet{|ds|  ^ (lookup ? -1) }

    prGetParams{^[this.numNeighbours,this.radius,this.lookupDataSet,-1,-1];}

    fitMsg{ |dataSet| ^this.prMakeMsg(\fit,this.id,dataSet.id);}

	fit{|dataSet,action|
        actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSet));
	}

    kNearestMsg{|buffer|
        ^this.prMakeMsg(\kNearest,id,this.prEncodeBuffer(buffer));
    }

	kNearest{ |buffer, action|
        actions[\kNearest] = [strings(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.kNearestMsg(buffer));
	}

    kNearestDistMsg {|buffer|
        ^this.prMakeMsg(\kNearestDist,id,this.prEncodeBuffer(buffer));
    }

	kNearestDist { |buffer, action|
        actions[\kNearestDist] = [numbers(FluidMessageResponse,_,nil,_),action];
		this.prSendMsg(this.kNearestDistMsg(buffer));
	}

    kr{|trig, inputBuffer,outputBuffer, numNeighbours = 1, lookupDataSet|
        this.numNeighbours_(numNeighbours);
        lookupDataSet = lookupDataSet ? -1;
        this.lookupDataSet_(lookupDataSet);
        this.lookupDataSet.asUGenInput.postln;
        ^FluidProxyUgen.kr('FluidKDTree/query', K2A.ar(trig),
                id, this.numNeighbours, this.radius, this.lookupDataSet.asUGenInput,
                this.prEncodeBuffer(inputBuffer),
                this.prEncodeBuffer(outputBuffer));
    }

}
