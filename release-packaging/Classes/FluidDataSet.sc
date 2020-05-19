
FluidDataSetExistsError : Exception{
}

FluidDataSet : FluidManipulationClient {

  var <id;
	classvar serverCaches;

	*initClass {
		serverCaches = FluidServerCache.new;
	}

	*at{ |server, id|
		^serverCaches.tryPerform(\at, server,id)
	}

  *new { |server,name|
		if(this.at(server,name).notNil){
			FluidDataSetExistsError("A FluidDataset called % already exists.".format(name)).throw;
			^nil
		}
		^super.new(server,*FluidManipulationClient.prServerString(name))!?{|inst|inst.init(name);inst}
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

  cols {|action|
		action ?? {action = postit};
      this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
  }

  read{|filename,action|
      this.prSendMsg(\read,[filename.asString],action);
  }

  write{|filename,action|
      this.prSendMsg(\write,[filename.asString],action);
  }

  size { |action|
		action ?? {action = postit};
      this.prSendMsg(\size,[],action,[numbers(FluidMessageResponse,_,1,_)]);
  }

  clear { |action|
      this.prSendMsg(\clear,[],action);
  }

	free {
		serverCaches.remove(server,id);
		super.free;
	}

	*freeAll { |server|
		serverCaches.do(server,{|x|x.free;});
	}
}
