
FluidDataSet : FluidDataObject
{
	*new{|server| ^super.new(server) }

	addPointMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\addPoint,id,identifier.asSymbol,buffer);
	}

	addPoint{|identifier, buffer, action|
		actions[\addPoint] = [nil,action];
		this.prSendMsg(this.addPointMsg(identifier,buffer));
	}

	getPointMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\getPoint,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	getPoint{|identifier, buffer, action|
		actions[\getPoint] = [nil,action];
		this.prSendMsg(this.getPointMsg(identifier,buffer));
	}

	updatePointMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\updatePoint,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	updatePoint{|identifier, buffer, action|
		actions[\updatePoint] = [nil,action];
		this.prSendMsg(this.updatePointMsg(identifier,buffer));
	}

	deletePointMsg{|identifier| ^this.prMakeMsg(\deletePoint,id,identifier.asSymbol);}

	deletePoint{|identifier, action|
		actions[\deletePoint] = [nil,action];
		this.prSendMsg(this.deletePointMsg(identifier));
	}

	setPointMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\setPoint,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	setPoint{|identifier, buffer, action|
		actions[\setPoint] = [nil,action];
		this.prSendMsg(this.setPointMsg(identifier,buffer));
	}

	clearMsg { ^this.prMakeMsg(\clear,id); }

	clear { |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	mergeMsg{|sourceDataSet, overwrite = 0|
		^this.prMakeMsg(\merge,id,sourceDataSet.asUGenInput,overwrite);
	}

	merge{|sourceDataSet, overwrite = 0, action|
		actions[\merge] = [nil,action];
		this.prSendMsg(this.mergeMsg(sourceDataSet,overwrite));
	}

	printMsg { ^this.prMakeMsg(\print,id); }

	print { |action=(postResponse)|
		actions[\print] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.printMsg);
	}

	toBufferMsg{|buffer, transpose = 0, labelSet|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\toBuffer, id, buffer, transpose, labelSet.asUGenInput,["/b_query",buffer.asUGenInput]);
	}

	toBuffer{|buffer, transpose = 0, labelSet, action|
		actions[\toBuffer] = [nil,action];
		this.prSendMsg(this.toBufferMsg(buffer, transpose, labelSet));
	}

	fromBufferMsg{|buffer, transpose = 0, labelSet|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\fromBuffer, id, buffer, transpose, labelSet.asUGenInput,["/b_query",buffer.asUGenInput]);
	}

	fromBuffer{|buffer, transpose = 0, labelSet, action|
		actions[\fromBuffer] = [nil,action];
		this.prSendMsg(this.fromBufferMsg(buffer, transpose, labelSet));
	}

	getIdsMsg{|labelSet|
		^this.prMakeMsg(\getIds, id, labelSet.asUGenInput);
	}

	getIds{|labelSet, action|
		actions[\getIds] = [nil,action];
		this.prSendMsg(this.getIdsMsg(labelSet));
	}
	
	kNearestMsg{|buffer,k|
	^this.prMakeMsg(\kNearest,id, this.prEncodeBuffer(buffer),k);
	}

	kNearest{ |buffer, k, action|
	actions[\kNearest] = [strings(FluidMessageResponse,_,_),action];
	this.prSendMsg(this.kNearestMsg(buffer,k));
	}

	kNearestDistMsg {|buffer, k|
	^this.prMakeMsg(\kNearestDist,id,this.prEncodeBuffer(buffer),k);
	}

	kNearestDist { |buffer, k, action|
		actions[\kNearestDist] = [numbers(FluidMessageResponse,_,nil,_),action];
		this.prSendMsg(this.kNearestDistMsg(buffer,k));
	}
}
