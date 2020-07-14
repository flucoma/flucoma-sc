
FluidDataSetExistsError : Exception{
}

FluidDataSet : FluidManipulationClient {

  var <id;
	classvar serverCaches;

	*initClass {
		serverCaches = FluidServerCache.new;
	}

	*at{ |server, name|
		^serverCaches.tryPerform(\at, server, name)
	}

	*new { |server, name|
		if(this.at(server,name).notNil){
			FluidDataSetExistsError("A FluidDataset called % already exists.".format(name)).throw;
			^nil
		}
		^super.new(server,FluidManipulationClient.prServerString(name))!?{|inst|inst.init(name);inst}
	}

	init {|name|
		id = name;
		this.cache;
	}

	cache {
		serverCaches.initCache(server);
		serverCaches.put(server,id,this);
	}

	*asUGenInput { |input|
		var ascii = input.asString.ascii;
		^[ascii.size].addAll(ascii)
	}

	asString {
		^"FluidDataSet(%)".format(id).asString;
	}

	asSymbol {
		^id.asSymbol
	}

	addPoint{|label, buffer, action|
	  this.prSendMsg(\addPoint,[label.asSymbol,buffer.asUGenInput],action);
	}

	getPoint{|label, buffer, action|
	  this.prSendMsg(\getPoint,[label.asSymbol,buffer.asUGenInput],action);
	}

	updatePoint{|label, buffer, action|
	  this.prSendMsg(\updatePoint,[label.asSymbol,buffer.asUGenInput],action);
	}

	deletePoint{|label, action|
	  this.prSendMsg(\deletePoint,[label.asSymbol],action);
	}

	clear { |action|
	  this.prSendMsg(\clear,[],action);
	}

	print { |action|
		action ?? {action = postit};
		this.prSendMsg(\print,[],action,[string(FluidMessageResponse,_,_)]);
	}

	free {
		serverCaches.remove(server,id);
		super.free;
	}

	*freeAll { |server|
		serverCaches.do(server,{|x|x.free;});
	}
}
