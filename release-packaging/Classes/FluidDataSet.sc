
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

  addPoint{|label, buffer, action|
      this.prSendMsg(\addPoint,[label.asString,buffer.asUGenInput],action);
  }

  getPoint{|label, buffer, action|
      this.prSendMsg(\getPoint,[label.asString,buffer.asUGenInput],action);
  }

  updatePoint{|label, buffer, action|
      this.prSendMsg(\updatePoint,[label.asString,buffer.asUGenInput],action);
  }

  deletePoint{|label, action|
      this.prSendMsg(\deletePoint,[label.asString],action);
  }

  cols {|action|
      this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
  }

  read{|filename,action|
      this.prSendMsg(\read,[filename.asString],action);
  }

  write{|filename,action|
      this.prSendMsg(\write,[filename.asString],action);
  }

  size { |action|
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
