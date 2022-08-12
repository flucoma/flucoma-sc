FluidDataSetQuery : FluidDataObject {
	*new{|server| ^super.new(server) }

	addColumnMsg { |column|
		^this.prMakeMsg(\addColumn,id,column);
	}

	addColumn{|column, action|
		actions[\addColumn] = [nil,action];
		this.prSendMsg(this.addColumnMsg(column));
	}

	addRangeMsg{|start,count|
		^this.prMakeMsg(\addRange,id,start,count);
	}

	addRange{|start, count, action|
		actions[\addRange] = [nil, action];
		this.prSendMsg(this.addRangeMsg(start, count));
	}

	filterMsg{|column, condition, value, action|
		^this.prMakeMsg(\filter,id,column,condition.asSymbol,value);
	}

	filter{|column, condition, value, action|
		actions[\filter] = [nil, action];
		this.prSendMsg(this.filterMsg(column, condition, value));
	}

	andMsg{ |column, condition, value|
		^this.prMakeMsg(\and,id,column, condition.asSymbol, value);
	}

	and{|column, condition, value, action|
		actions[\and] = [nil, action];
		this.prSendMsg(this.andMsg(column,condition,value));
	}

	orMsg{|column, condition, value|
		^this.prMakeMsg(\or,id,column, condition.asSymbol, value)
	}

	or{|column, condition, value, action|
		actions[\or] = [nil,action];
		this.prSendMsg(this.orMsg(column, condition, value));
	}

	clearMsg{
		^this.prMakeMsg(\clear,id);
	}

	clear{|action|
		actions[\clear] = [nil, action];
		this.prSendMsg(this.clearMsg);
	}

	limitMsg{|rows|
		^this.prMakeMsg(\limit,id,rows);
	}

	limit{|rows, action|
		actions[\limit] = [nil,action];
		this.prSendMsg(this.limitMsg(rows));
	}

	transformMsg{|sourceDataSet, destDataSet|
		^this.prMakeMsg(\transform,id,sourceDataSet.id,destDataSet.id);
	}

	transform{|sourceDataSet, destDataSet, action|
		actions[\transform] = [nil,action];
		this.prSendMsg(this.transformMsg(sourceDataSet,destDataSet));
	}

	transformJoinMsg{|source1DataSet, source2DataSet, destDataSet|
		^this.prMakeMsg(\transformJoin,id,source1DataSet.id, source2DataSet.id, destDataSet.id);
	}

	transformJoin{|source1DataSet, source2DataSet, destDataSet, action|
		actions[\transformJoin] = [nil,action];
		this.prSendMsg(this.transformJoinMsg(source1DataSet, source2DataSet, destDataSet));
	}
}
