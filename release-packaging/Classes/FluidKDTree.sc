FluidKDTree : FluidModelObject
{

	var neighbours,radius;

	*new{ |server, numNeighbours = 1, radius = 0|
		^super.new(server,[numNeighbours,radius ? -1])
		.numNeighbours_(numNeighbours)
		.radius_(radius);
	}

	numNeighbours_{|k|neighbours = k.asInteger; }
	numNeighbours{ ^neighbours; }

	radius_{|r| radius = r.asUGenInput;}
	radius{ ^radius; }

	prGetParams{^[this.id, this.numNeighbours,this.radius];}

	fitMsg{ |dataSet| ^this.prMakeMsg(\fit,this.id,dataSet.id);}

	fit{|dataSet,action|
		actions[\fit] = [nil,action];
		this.prSendMsg(this.fitMsg(dataSet));
	}

	kNearestMsg{|buffer,k|
		k !?
		{^this.prMakeMsg(\kNearest,id,this.prEncodeBuffer(buffer),k);}
		??
		{^this.prMakeMsg(\kNearest,id,this.prEncodeBuffer(buffer));}
	}

	kNearest{ |buffer, k, action|
		actions[\kNearest] = [strings(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.kNearestMsg(buffer,k));
	}

	kNearestDistMsg {|buffer, k|
		k !?
		{^this.prMakeMsg(\kNearestDist,id,this.prEncodeBuffer(buffer),k);}
		??
		{^this.prMakeMsg(\kNearestDist,id,this.prEncodeBuffer(buffer));}
	}

	kNearestDist { |buffer, k, action|
		actions[\kNearestDist] = [numbers(FluidMessageResponse,_,nil,_),action];
		this.prSendMsg(this.kNearestDistMsg(buffer,k));
	}

	kr{|trig, inputBuffer, outputBuffer, numNeighbours, radius, lookupDataSet|
		^FluidKDTreeQuery.kr(trig,
			this, numNeighbours??{this.numNeighbours}, radius??{this.radius}, lookupDataSet.asUGenInput,
			inputBuffer,outputBuffer);
	}

}

FluidKDTreeQuery : FluidRTMultiOutUGen
{
	*kr{ |trig, tree, numNeighbours, radius, lookupDataSet, inputBuffer, outputBuffer |
		^this.multiNew('control', trig, tree.asUGenInput, numNeighbours, radius, lookupDataSet!?(_.asUGenInput)??{-1}, inputBuffer.asUGenInput, outputBuffer.asUGenInput)
	}

	init { arg ... theInputs;
		inputs = theInputs;
		^this.initOutputs(1, rate);
	}

}
