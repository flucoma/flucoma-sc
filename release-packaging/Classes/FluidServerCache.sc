FluidProxyUgen : UGen {

	var <>pluginname;

	*kr { |pluginname...args|
		args = args.collect{|x| x.asUGenInput}
		^this.new1('control', pluginname,*args)
	}

	init { |pluginname...args|
		this.pluginname = pluginname;
		inputs = args;
		rate = 'control';
	}

	name{
		^pluginname.asString;
	}

	poll{ |trig = 10, label, trigid = -1|
		^super.poll(trig, label ? this.name, trigid)
	}
}

FluidServerCache {

	var <cache;

	*new{ ^super.new.init }

	init{
		cache = IdentityDictionary.new;
	}

	do { |server, func|
		cache[server]!?{cache[server].do{|x|func.value(x)}}
	}

	doAll {|func|
		cache.do{|subCache|
			subCache.do{|item|
				func.value(item)
			}
		}
	}

	postln{
		cache.postln;
	}

	at { |server,id|
		^cache[server].tryPerform(\at,id)
	}

	includesKey{|server,key|
		^cache[server].tryPerform(\includesKey,key)
	}

	put {|server,id,x|
		cache[server][id] = x;
	}

	remove { |server,id|
		cache[server]!? {cache[server].removeAt(id)};
	}

	initCache {|server|
		cache[server] ?? {
			cache[server] = IdentityDictionary.new;
			NotificationCenter.register(server,\newAllocators,this,
				{
					this.clearCache(server);
			});
		}
	}

	clearCache { |server|
		cache[server] !?
		{
			var bundle = [];
			cache[server].values.do{|i|
				if(i.respondsTo(\freeMsg)){
					bundle = bundle.add(i.freeMsg); //server objects
				}{
					i.free; //OSCFunc
				}
			};
			server.listSendBundle(nil,bundle);
			cache.removeAt(server);
		};
	}

}
