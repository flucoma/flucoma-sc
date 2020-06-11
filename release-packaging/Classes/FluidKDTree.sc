FluidKDTree : FluidManipulationClient {

	var id;

	*new {|server|
		var uid = UniqueID.next;
		^super.new(server,uid)!?{|inst|inst.init(uid);inst}
	}

	init {|uid|
		id = uid;
	}

	fit{|dataset,action|
		dataset.asSymbol.postln;
		this.prSendMsg(\fit, [dataset.asSymbol], action);
	}

	kNearest{ |buffer, k,action|
		this.prSendMsg(\kNearest,
			[buffer.asUGenInput,k], action,
			k.collect{string(FluidMessageResponse,_,_)}
		);
	}

	kNearestDist { |buffer, k,action|
		this.prSendMsg(\kNearestDist, [buffer.asUGenInput,k], action,
			[numbers(FluidMessageResponse,_,k,_)]
		);
	}
}
