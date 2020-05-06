FluidLabelSetExistsError : Exception{
}

FluidLabelSet : FluidManipulationClient {

	var  <id;
	classvar serverCaches;

	*initClass {
		serverCaches = FluidServerCache.new;
	}

	*at{ |server, id|
		^serverCaches.tryPerform(\at, server,id)
	}

  *new { |server,name|
		serverCaches.at(server,name) !? {
			FluidLabelSetExistsError("A FluidLabelSet called % already exists.".format(name)).throw;
		};
		^super.new(server,*FluidManipulationClient.prServerString(name)).init(name)
  }

  init { |name|
    this.id = name;
		this.cache;
  }

	cache {
		serverCaches.initCache(server);
		serverCaches.put(server,id,this);
	}

  asString {
     	^"FluidLabelSet(%)".format(id).asString;
  }

	asUGenInput{
		^id.asString;
	}

  addLabel{|id, label, action|
      this.prSendMsg(\addLabel,[id.asString, label.asString],action);
  }

  getLabel{|id, action|
      this.prSendMsg(\getLabel,[id.asString],action,[string(FluidMessageResponse,_,_)]);
  }

  deleteLabel{|id, action|
      this.prSendMsg(\deleteLabel,[id.asString],action);
  }

  cols {|action|
      this.prSendMsg(\cols,[],action,[number(FluidMessageResponse,_,_)]);
  }

  read{|filename,action|
      this.prSendMsg(\read,[filename.asString],action);
  }

  write{|filename,action|
      this.prSendMsg(\write,[filename.asString],action);
  }

  size { |action|
      this.prSendMsg(\size,[],action,[number(FluidMessageResponse,_,_)]);
  }

  clear { |action|
      this.prSendMsg(\clear,[],action);
  }

	free { |action|
		serverCaches.remove(server,id);
		if(server.serverRunning){this.prSendMsg(\free,[],action)};
		super.free;
	}
	
	*freeAll { |server|
		serverCaches.tryPerform(\clearCache,server);
	}
}   
