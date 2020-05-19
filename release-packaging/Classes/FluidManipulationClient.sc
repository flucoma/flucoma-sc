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

	var postit;
	var < ready;

	*prServerString{ |s|
		var ascii = s.ascii;
		^[ascii.size].addAll(ascii)
	}

	*newFromDesc { arg rate, numOutputs, inputs, specialIndex;
		^FluidProxyUgen.newFromDesc(rate, numOutputs, inputs, specialIndex)
	}


	*new{ |server...args|
		server = server ? Server.default;
		if(server.serverRunning.not,{
			(this.asString + "– server not running").error; ^nil
		});
		^super.newCopyArgs(server ?? {Server.default}).baseinit(*args)
	}

	baseinit { |...args|
		var makeFirstSynth;
		id = UniqueID.next;
		postit = {|x| x.postln;};
		defName = (this.class.name.asString ++ id).asSymbol;
		ready = Condition(false);
		def = SynthDef(defName,{
			var  ugen = FluidProxyUgen.kr(this.class.name, *args);
			this.ugen = ugen;
			ugen
		});

		persist = true;
		onSynthFree = {
			ready.test = false;
			synth = nil;
			if(persist){
				//If we don't sync here, cmd-. doesn't reset properly (but server.freeAll does)
				forkIfNeeded {
					server.sync;
					synth = Synth(defName,target: RootNode(server));
					synth.onFree{onSynthFree.value};
					ready.test = true;
					ready.signal;
				}
			}
		};
		forkIfNeeded{
			def.add;
			server.sync;
			onSynthFree.value;
		}
	}

	free{
		persist = false;
		if(server.serverRunning){server.sendMsg("/cmd","free"++this.class.name,id)};
		synth.tryPerform(\free);
		^nil
	}

	prSendMsg { |msg, args, action,parser|
		if(this.server.serverRunning.not,{(this.asString + "– server not running").error; ^nil});
		synth ?? {"Not ready".warn};
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

	do { |server, func|
		cache[server]!?{cache[server].do{|x|func.value(x)}}
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
		cache[server] !? { cache.removeAt(server) };
	}

}

