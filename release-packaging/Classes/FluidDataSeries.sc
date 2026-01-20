
FluidDataSeries : FluidDataObject
{
	*new{|server| ^super.new(server) }

	addSeriesMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\addSeries,id,identifier.asSymbol,buffer);
	}

	addSeries{|identifier, buffer, action|
		actions[\addSeries] = [nil,action];
		this.prSendMsg(this.addSeriesMsg(identifier,buffer));
	}

	getSeriesMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\getSeries,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	getSeries{|identifier, buffer, action|
		actions[\getSeries] = [nil,action];
		this.prSendMsg(this.getSeriesMsg(identifier,buffer));
	}

	updateSeriesMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\updateSeries,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	updateSeries{|identifier, buffer, action|
		actions[\updateSeries] = [nil,action];
		this.prSendMsg(this.updateSeriesMsg(identifier,buffer));
	}

	setSeriesMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\setSeries,id,identifier.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
	}

	setSeries{|identifier, buffer, action|
		actions[\setSeries] = [nil,action];
		this.prSendMsg(this.setSeriesMsg(identifier,buffer));
	}

	deleteSeriesMsg{|identifier| ^this.prMakeMsg(\deleteSeries,id,identifier.asSymbol);}

	deleteSeries{|identifier, action|
		actions[\deleteSeries] = [nil,action];
		this.prSendMsg(this.deleteSeriesMsg(identifier));
	}

	addFrameMsg{|identifier,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\addFrame,id,identifier.asSymbol,buffer);
	}

	addFrame{|identifier, buffer, action|
		actions[\addFrame] = [nil,action];
		this.prSendMsg(this.addFrameMsg(identifier,buffer));
	}

	getFrameMsg{|identifier,time,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\getFrame,id,identifier.asSymbol,time.asInteger,buffer,["/b_query",buffer.asUGenInput]);
	}

	getFrame{|identifier, time, buffer, action|
		actions[\getFrame] = [nil,action];
		this.prSendMsg(this.getFrameMsg(identifier,time,buffer));
	}

	updateFrameMsg{|identifier,time,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\updateFrame,id,identifier.asSymbol,time.asInteger,buffer,["/b_query",buffer.asUGenInput]);
	}

	updateFrame{|identifier, time, buffer, action|
		actions[\updateFrame] = [nil,action];
		this.prSendMsg(this.updateFrameMsg(identifier,time,buffer));
	}

	setFrameMsg{|identifier,time,buffer|
		buffer = this.prEncodeBuffer(buffer);
		^this.prMakeMsg(\setFrame,id,identifier.asSymbol,time.asInteger,buffer,["/b_query",buffer.asUGenInput]);
	}

	setFrame{|identifier, time, buffer, action|
		actions[\setFrame] = [nil,action];
		this.prSendMsg(this.setFrameMsg(identifier,time,buffer));
	}

	deleteFrameMsg{|identifier,time| ^this.prMakeMsg(\deleteFrame,id,identifier.asSymbol,time.asInteger);}

	deleteFrame{|identifier, time, action|
		actions[\deleteFrame] = [nil,action];
		this.prSendMsg(this.deleteFrameMsg(identifier,time));
	}

	clearMsg { ^this.prMakeMsg(\clear,id); }

	clear { |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	mergeMsg{|sourceDataSeries, overwrite = 0|
		^this.prMakeMsg(\merge,id,sourceDataSeries.asUGenInput,overwrite);
	}

	merge{|sourceDataSeries, overwrite = 0, action|
		actions[\merge] = [nil,action];
		this.prSendMsg(this.mergeMsg(sourceDataSeries,overwrite));
	}

	printMsg { ^this.prMakeMsg(\print,id); }

	print { |action=(postResponse)|
		actions[\print] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.printMsg);
	}

	getIdsMsg{|labelSet|
		^this.prMakeMsg(\getIds, id, labelSet.asUGenInput);
	}

	getIds{|labelSet, action|
		actions[\getIds] = [nil,action];
		this.prSendMsg(this.getIdsMsg(labelSet));
	}

	getDataSetMsg{|time, destDataSet|
		^this.prMakeMsg(\getDataSet, id, time, destDataSet.asUGenInput);
	}

	getDataSet{|time, destDataSet, action|
		actions[\getDataSet] = [nil,action];
		this.prSendMsg(this.getDataSetMsg(time, destDataSet));
	}

	kNearestMsg{|buffer,k|
		^this.prMakeMsg(\kNearest,id, this.prEncodeBuffer(buffer),k);
	}

	kNearest{ |buffer, k, action|
		actions[\kNearest] = [strings(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.kNearestMsg(buffer,k));
	}

	kNearestDistMsg{|buffer,k|
		^this.prMakeMsg(\kNearestDist,id, this.prEncodeBuffer(buffer),k);
	}

	kNearestDist{ |buffer, k, action|
		actions[\kNearestDist] = [strings(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.kNearestDistMsg(buffer,k));
	}
}
