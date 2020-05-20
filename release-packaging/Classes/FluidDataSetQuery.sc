
FluidDataSetQuery : FluidManipulationClient {

  *new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

	addColumn{|column, action|
		this.prSendMsg(\addColumn, [column], action);
	}


	addRange{|start, count, action|
		this.prSendMsg(\addRange, [start, count], action);
	}


	filter{|column, condition, value, action|
		this.prSendMsg(\filter, [column, condition.asSymbol, value], action);
	}


	and{|column, condition, value, action|
		this.prSendMsg(\and, [column, condition, value], action);
	}

	or{|column, condition, value, action|
		this.prSendMsg(\or, [column, condition, value], action);
	}

	reset{|action|
		this.prSendMsg(\reset, [], action);
	}

	limit{|rows, action|
		this.prSendMsg(\limit, [rows], action);
	}

    transform{|sourceDataset, destDataset, action|
        this.prSendMsg(\transform,[sourceDataset.asSymbol, destDataset.asSymbol],action);
    }

}
