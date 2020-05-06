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

    var  <synth,gen;
	var  bootFunc;
	var  synthDefLoaded,id, defName, <>ugen, updateFunc;
	var  nodeResponder;

	var initTreeCondition;
	var synthBeenSet = false;
	var serverListener;

	*prServerString{ |s|
		var ascii = s.ascii;
		^[ascii.size].addAll(ascii)
	}

	sendSynthDef { |...args|
		var plugin = this.class.name.asSymbol;
		if(server.hasBooted)
		{
			fork{
				SynthDef(defName.asSymbol,{
					var  ugen = FluidProxyUgen.kr(plugin, *args);
					this.ugen = ugen;
					ugen
				}).send(server);

				server.sync;

				synthDefLoaded = true;
				updateFunc = {
					//Sometimes Server.initTree seems to get called a bunch of
					//times during boot: we can't be having extra instances
					//However, once boot has finished, ending up here means cmd-. or server.freeAll
					//has happened, and we just need to run

					var shouldRun = (synthBeenSet.not.and(server.serverBooting))
					.or(server.serverRunning.and(server.serverBooting.not));

					if(shouldRun) {
						synthBeenSet = true;
						synth = nil;
						this.updateSynth;
					}
				};
				updateFunc.value;
				ServerTree.add(updateFunc, server);
			};
		};
	}

	updateSynth {
		if(server.hasBooted){
			if(synthDefLoaded){
				if(synth.isNil){
					synth = Synth.after(server.defaultGroup,defName.asSymbol);
					synth.register;
				}
			}
		}{
			synth !? {synth.free};
		}
	}

    *new{ |server...args|
        server = server ? Server.default;
		if(server.serverRunning.not,{
			(this.asString + "– server not running").warn;

		});
		^super.newCopyArgs(server ?? {Server.default}).baseinit(*args)
    }

	baseinit { |...args|

		id = UniqueID.next;
		synthDefLoaded = false;
		defName = (this.class.name.asString ++ id);

		if(server.serverRunning){ this.sendSynthDef(*args);};

		bootFunc = {
			ServerBoot.remove(bootFunc,server);
			synth = nil;
			this.sendSynthDef(*args);
		};

		ServerBoot.add(bootFunc,server);
		ServerQuit.add({this.free;},server);
	}

	free{
		ServerTree.remove(updateFunc,server);
		ServerBoot.remove(bootFunc, server);
		updateFunc = nil;
		// synth !? {synth.tryPerform(\free)};//
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

