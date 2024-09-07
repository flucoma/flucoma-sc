FluidDTW : FluidModelObject {
	
	var <>constraint, <>constraintParam;
	
	*new {|server, constraint = 0, constraintParam = 0|
		^super.new(server,[constraint, constraintParam])
		.constraint_(constraint)
		.constraintParam_(constraintParam);
	}
	
	prGetParams{^[this.id,this.constraint,this.constraintParam];}
	
	costMsg{|dataSeries, id1, id2|
		^this.prMakeMsg(\cost, this.id, dataSeries.id, id1.asSymbol, id2.asSymbol)
	}
	
	cost{|dataSeries, id1, id2, action|
		actions[\cost] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.costMsg(dataSeries, id1.asSymbol, id2.asSymbol));
	}
	
	bufCostMsg{|buffer1, buffer2|
		^this.prMakeMsg(\bufCost, this.id,
			this.prEncodeBuffer(buffer1),
			this.prEncodeBuffer(buffer2));
	}
	
	bufCost{|buffer1, buffer2, action|
		actions[\bufCost] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.bufCostMsg(buffer1,buffer2));
	}
}
