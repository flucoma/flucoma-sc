FluidProxyUgen : UGen {

	var <>pluginname;

	*kr { |pluginname...args|
		^this.new1('control', pluginname,*args)
	}

	init { |pluginname...args|
		this.pluginname = pluginname;
		inputs = args++Done.none++0;
	}

	name{
		^pluginname.asString;
	}
}

FluidManipulationClient {

	var <server;
	var  <synth,<>ugen;
	var  id;
	var defName, def;
	var onSynthFree, persist;

	*prServerString{ |s|
		var ascii = s.ascii;
		^[ascii.size].addAll(ascii)
	}

	*new{ |server...args|
		server = server ? Server.default;
		if(server.serverRunning.not,{
			(this.asString + "– server not running").warn;
		});
		^super.newCopyArgs(server ?? {Server.default}).baseinit(*args)
	}

	baseinit { |...args|
		var makeFirstSynth;
		id = UniqueID.next;
		defName = (this.class.name.asString ++ id).asSymbol;

		def = SynthDef(defName,{
			var  ugen = FluidProxyUgen.kr(this.class.name, *args);
			this.ugen = ugen;
			ugen
		});

		synth = Synth.basicNew(defName,server);
		persist = true;
		onSynthFree = {
			if(persist){
				synth = Synth.after(server.defaultGroup,defName);
				synth.onFree(onSynthFree);
			}
		};

		synth.onFree(onSynthFree);
		CmdPeriod.add(onSynthFree);

		makeFirstSynth ={
			var synthMsg= synth.newMsg(server.defaultGroup,\addAfter);
			def.send(server,synthMsg);
		};

		if(server.serverRunning)
		{ makeFirstSynth.value}
		{server.doWhenBooted(makeFirstSynth)};
	}

	free{
		persist = false;
		CmdPeriod.remove(onSynthFree);
		synth = nil;
	}

	prSendMsg { |msg, args, action,parser|
		if(this.server.serverRunning.not,{(this.asString + "– server not running").error; ^nil});
		synth !? {
			OSCFunc(
				{ |msg|
					forkIfNeeded{
						var result;
						result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
						if(action.notNil){action.value(result)}{action.value};
					}
			},'/'++msg, server.addr, nil,[synth.nodeID]).oneShot;
			server.listSendMsg(['/u_cmd',synth.nodeID,ugen.synthIndex,msg].addAll(args));
		}
	}
}

FluidServerCache {

	var cache;

	*new{ ^super.new.init }

	init{
		cache = IdentityDictionary.new;
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
			ServerQuit.add({this.clearCache(server)},server);
			NotificationCenter.register(server,\newAllocators,this, {
				this.clearCache(server);
			});
		}
	}

	clearCache { |server|
		cache[server] !? { cache.removeAt(server) !? {|x| x.tryPerform(\free) } };
	}
}

