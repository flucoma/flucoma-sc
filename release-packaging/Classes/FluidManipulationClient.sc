FluidProxyUgen : UGen {

	var <>pluginname;

	*kr { |pluginname...args|
		^this.new1('control', pluginname,*args)
	}

	init { |pluginname...args|
		this.pluginname = pluginname;
		inputs = args;
		pluginname
		.asSymbol
		.asClass
		.superclasses
		.indexOf(FluidRTDataClient) ??{inputs= inputs ++ [Done.none,0]};
		rate = 'control';
	}

	name{
		^pluginname.asString;
	}
}

FluidManipulationClient {

	classvar clock;

	var <server;
	var <synth,<>ugen;
	var id;
	var defName, def;
	var onSynthFree, keepAlive;
	var aliveThread;
	var postit;

	*initClass {
		clock = TempoClock.new;
		clock.permanent = true;
	}

	*prServerString{ |s|
		var ascii = s.ascii;
		^[ascii.size].addAll(ascii)
	}

	*newFromDesc { arg rate, numOutputs, inputs, specialIndex;
		^FluidProxyUgen.newFromDesc(rate, numOutputs, inputs, specialIndex)
	}

	*new{ |server,objectID...args|
		server = server ? Server.default;
		if(server.serverRunning.not,{
			(this.asString + "– server not running").error; ^nil
		});
		^super.newCopyArgs(server ?? {Server.default}).baseinit(objectID,*args)
	}

	makeDef { |defName,objectID,args|
		var initialVals = [];
		args!? { if(args.size > 0) {initialVals = args.unlace(2)[1].flatten}};
		^SynthDef(defName,{
			var  ugen = FluidProxyUgen.kr(this.class.name, *(initialVals ++ objectID));
			this.ugen = ugen;
			ugen
		});
	}

	updateSynthControls {}

	baseinit { |objectID...args|
		var makeFirstSynth,synthMsg,synthArgs;
		id = UniqueID.next;
		postit = {|x| x.postln;};
		keepAlive = true;
		defName = (this.class.name.asString ++ id).asSymbol;
		def = this.makeDef(defName,objectID,args);
		synth = Synth.basicNew(def.name, server);
		synthMsg = synth.newMsg(RootNode(server),args);
		def.doSend(server,synthMsg);

		onSynthFree = {
			synth = nil;
			if(keepAlive){
				synth = Synth(defName,target: RootNode(server));
				synth.onFree{clock.sched(0,onSynthFree)};
				this.updateSynthControls;
			}
		};
		CmdPeriod.add({synth = nil});
		synth.onFree{clock.sched(0,onSynthFree)};
	}

	free{
		keepAlive = false;
		if(server.serverRunning){server.sendMsg("/cmd","free"++this.class.name,id)};
		synth.tryPerform(\free);
		^nil
	}

	cols {|action|
		action ?? {action = postit};
	  this.prSendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
	}

	read{|filename, action|
	  this.prSendMsg(\read,[filename.asString],action);
	}

	write{|filename, action|
	  this.prSendMsg(\write,[filename.asString],action);
	}

	size {|action|
		action ?? {action = postit};
	  this.prSendMsg(\size,[],action,[numbers(FluidMessageResponse,_,1,_)]);
	}

	prSendMsg { |msg, args, action,parser|
		if(this.server.serverRunning.not,{(this.asString + "– server not running").error; ^nil});
		forkIfNeeded{
			synth ?? {onSynthFree.value; server.sync};
			OSCFunc(
				{ |msg|
					defer{
						var result;
						result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
						if(action.notNil){action.value(result)}{action.value};
					}
			},'/'++msg, server.addr, nil,[synth.nodeID]).oneShot;
			server.listSendMsg(['/u_cmd', synth.nodeID, ugen.synthIndex, msg].addAll(args));
		}
	}
}

FluidDataClient : FluidManipulationClient {

	classvar synthControls = #[];

	var <id;
	var parameters;
	var parameterDefaults;

	*new {|server|
		^this.new1(server,#[])
	}

	*new1{ |server, params|
		var uid = UniqueID.next;
		params = params ?? {[]};
		if(params.size > 0 and: synthControls.size == 0) {synthControls = params.unlace[0]};
		^super.new(server, uid, *params) !? { |inst| inst.init(uid, params) }
	}

	init { |uid, params|
		id = uid;
		parameters = ().putPairs(params);
		parameterDefaults = parameters.copy;
		this.makePropertyMethods;
	}

	makePropertyMethods{
		if (parameters.keys.size > 0) {
			parameters.keys.do{|c,i|
				this.addUniqueMethod(c,{ parameters.at(c) });
				this.addUniqueMethod((c++\_).asSymbol,{|responder,x|
					//if we have a default or initial value set, then fall back to
					//this if val is nil. Otherwise, fallback even furter to -1 as
					// a best guess
					x = x ?? { parameterDefaults !? { parameterDefaults[c] } ?? {-1} };
					parameters.put(c, x.asUGenInput);
					synth !? { if(synth.isRunning){ synth.set(c,x); } };
					responder
				});
			}
		};
	}

	updateSynthControls{
		synth !? { synth.set(*parameters.asKeyValuePairs); };
	}
}

FluidRTDataClient : FluidDataClient
{

	*new1{|server, params|
		params = params ?? {[]};
		if(params.size > 0) {synthControls = params.unlace[0]};
		params = params ++ [\inBus,Bus.control,\outBus,Bus.control,\inBuffer,-1,\outBuffer,-1];
		^super.new1(server,params)
	}

	makeDef {|defName,uid,args|
		var defControls = [\inBus, \outBus] ++ synthControls ++ [\inBuffer,\outBuffer];
		var ugenControls = [this.class.name,"T2A.ar(In.kr(inBus))"] ++ synthControls ++ [\inBuffer,\outBuffer,uid];
		var f = (
			"{ |dataClient|"
			"    SynthDef("++defName.asCompileString++", { |" ++ defControls.join(",") ++ "|"
			"       var  ugen = FluidProxyUgen.kr(" ++ ugenControls.join(",") ++ ");"
			"	    dataClient.ugen = ugen;"
			"       Out.kr(outBus,ugen);"
			"     })"
			"}"
		);
		var res = f.interpret.value(this);
		^res
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
