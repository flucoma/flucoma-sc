FluidServerObject
{
	classvar serverCaches;
	classvar count;
	classvar persistent = true;
	var <server,<id;

	*version{|server|
		server ?? {server = Server.default};
		server.sendMsg("/cmd","/"++this.objectClassName++'/version');
	}

	*initClass {
		serverCaches = IdentityDictionary.new;
		count = 0;
		ServerBoot.add({serverCaches[this]!?{serverCaches[this].cache.put(Server.internal,nil);}},Server.internal);
	}

	*initCache {|server|
		serverCaches[this] ?? { serverCaches[this] = FluidServerCache.new};

		if(server === Server.internal and: serverCaches[this].cache[Server.internal].isNil)
		{
			this.flush(Server.internal)
		};

		serverCaches[this].initCache(server);
		NotificationCenter.register(server,\newAllocators,this,{ count = 0; });
	}

	*newMsg{|id, params|
		params = params !? {params.collect(_.asUGenInput)};
		^this.prMakeMsg(\new,id,*params);
	}

	*new{ |server, id, params, action, callNew = true|
		var newObj;
		server ?? {server = Server.default};
		if(server.serverRunning.not){"Server not running".warn};
		id !? { id = id.asInteger }
		?? { id = count; count = count + 1; };
		newObj = super.newCopyArgs(server,id,action);
		// params.postln;
		if(callNew) {server.listSendMsg(this.newMsg(id,params))};
		^newObj.cache
	}

	cache {
		this.class.initCache(server);
		serverCaches[this.class].put(server,this.id,this);
	}

	uncache{
		serverCaches[this.class].remove(server,id);
	}

	*prMakeMsg{|msg,id...args|
		var commandName = "%/%".format(this.objectClassName,msg);
		^['/cmd', this.objectClassName,commandName,id].addAll(args);
	}

	prMakeMsg{|msg,id...args| ^this.class.prMakeMsg(msg,id,*args) }

	freeMsg {
		var msg;
		id ?? {" % already freed".format(this.class.name).warn; ^nil};
		this.uncache;
		msg = this.prMakeMsg(\free,id);
		id = nil;
		^msg;
	}

	free{
		var msg = this.freeMsg;
		msg !? {server.listSendMsg(msg)} ?? {^nil};
	}

	*freeAll{|server|
		serverCaches[this] !? {|cache|
			cache.clearCache(server ? Server.default);
		};
		count = 0;
	}

	asUGenInput{ ^id }

	asString {
		^"%(%)".format(this.class.name,id).asString;
	}

	asSymbol {
		^id.asSymbol
	}

	*objectClassName { ^this.name.asSymbol }

	*flushMsg { ^['/cmd',this.objectClassName ++ '/flush'] }

	*flush {|server| server.listSendMsg(this.flushMsg)}
}

FluidBufProcessor : FluidServerObject
{
	var <processAction;
	var <outputBuffers;
	var <freeWhenDone;
	classvar responder;
	classvar count;

	*cmdPeriod {
		serverCaches[this] !? {|cache|
			cache.doAll{|processor| processor !? { processor.free;} };
			serverCaches[this] = nil;
		};
		count = 0;
	}

	*initCache {|server|
		// "initcache".postln;
		// this.done.postln;
		super.initCache(server);
		CmdPeriod.add(this);
		if(serverCaches[this].includesKey(server,\processResponder).not)
		{
			serverCaches[this].put(server,\processResponder,OSCFunc({|m|
				var id = m.last.asInteger;
				// "I'm in the pizza hut".postln;
				serverCaches[this].at(server,id) !? {|p|
					// "I'm in the taco bell".postln ;
					p!?{
						p.processAction!?{|a|
							var bufs = p.outputBuffers;

							bufs = bufs.collect{|b|
								if(b.isKindOf(Buffer))
								{b}
								{Buffer.cachedBufferAt(server,b)};
							};
							a.valueArray(valueArray(bufs));
						};
						if(p.freeWhenDone){p.free};
					}
				}
			},this.done ,server.addr).fix)
		}
	}

	*new {|server,id,outputBuffers|
		^super.new(server,id, nil, nil,false).init(outputBuffers);
	}

	init{ |ob|
		outputBuffers = ob;
	}

	*done {
		^"/%/process".format(this.objectClassName);
	}

	wait {
		var condition = Condition.new;
		id ?? {Error("% already freed".format(this.class.name)).throw};
		OSCFunc({
			condition.unhang;
		},this.class.done,server.addr,argTemplate:[nil,id]).oneShot;
		condition.hang;
	}

	processMsg {|params|
		var msg;
		var completionMsg = outputBuffers !?    {
			[["/sync"]] ++ outputBuffers.collect{|b| ["/b_query", b.asUGenInput]}
		} ?? {[]};

		// completionMsg.postln;
		id ?? {Error("% already freed".format(this.class.name)).throw};
		msg = this.prMakeMsg(\processNew,id).addAll(params).add(completionMsg);
		// msg.postln;
		^msg;
	}

	processList { |params,shouldFree,action|
		freeWhenDone = shouldFree;
		processAction = action;
		params = params.collect(_.asUGenInput);
		server.listSendMsg(this.processMsg(params));
	}

	cancelMsg{
		id ?? {Error("% already freed".format(this.class.name)).throw};
		^this.prMakeMsg(\cancel, id);
	}

	cancel{
		server.listSendMsg(this.cancelMsg);
	}

	kr{  ^FluidProxyUgen.kr(this.class.objectClassName ++ "Monitor",id) }
}

FluidOSCPatternInversion : OSCMessageDispatcher
{
	value {|msg, time, addr, recvPort|
		var msgpath = msg[0].asSymbol;
		active.keysValuesDo({|key, func|
			if(msgpath.matchOSCAddressPattern(key), {func.value(msg, time, addr, recvPort);});
		})
	}
}


FluidDataObject : FluidServerObject
{
	classvar postResponse;

	var <actions;

	*initClass{
		postResponse = _.postln;
	}

	*initCache{ |server|
		super.initCache(server);
		if(serverCaches[this].includesKey(server,\messageResponder).not)
		{
			serverCaches[this].put(server,\messageResponder,OSCFunc.new({|m|
				var id = m[1].asInteger;
				var method;
				serverCaches[this].at(server,id) !? { |p|
					method = m[0].asString.findRegexp("/"++this.name++"/(.*)")[1][1].asSymbol;
					p.actions[method] !? {|a|
						//two items: parser and action
						var parser = a[0];
						var action = a[1];
						var result = FluidMessageResponse.collectArgs(parser,m[2..]);
						action.value(result);
					}
				}
			},'/' ++ this.objectClassName ++ '/*',server.addr, dispatcher:FluidOSCPatternInversion.new).fix)
		}
	}

	*new{|server...args|
		// args.flatten.postln;
		^super.new(server,params:args.flatten).init;
	}

	*cachedInstanceAt{|server,id|
		this.initCache(server);
		^serverCaches[this].at(server,id);
	}

	init {
		actions = IdentityDictionary.new;
	}

	prEncodeBuffer { |buf| buf !? {^buf.asUGenInput} ?? {^-1} }

	prSendMsg {|msg| server !? {server.listSendMsg(msg)};}

	colsMsg { ^this.prMakeMsg(\cols,id);}

	cols{ |action=(postResponse)|
		actions[\cols] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.colsMsg)
	}

	readMsg { |filename| ^this.prMakeMsg(\read,id,filename.asString);}

	read{|filename, action|
		actions[\read] = [nil,action];
		this.prSendMsg(this.readMsg(filename));
	}

	writeMsg {|filename|
		// ^['/cmd',this.class.name ++ '/write',id,filename.asString]
		^this.prMakeMsg(\write,id,filename.asString);
	}

	write{|filename, action|
		actions[\write] = [nil,action];
		this.prSendMsg(this.writeMsg(filename));
	}

	sizeMsg{
		// ^['/cmd',this.class.name ++ '/size',id]
		^this.prMakeMsg(\size,id);
	}

	size {|action=(postResponse)|
		actions[\size] = [numbers(FluidMessageResponse,_,1,_),action];
		this.prSendMsg(this.sizeMsg);
	}
}

FluidModelObject : FluidDataObject
{
	prGetParams{
		"Subclass should provide this".throw;
	}

	prUpdateStateMsg{
		var params = this.prGetParams.value.collect(_.asUGenInput);
		^this.prMakeMsg(\setParams,id) ++ params;
	}

	prSendMsg {|msg|
		//These need to happen sequentially, but not simultaneously
		//sending as a bundle makes reasoning about timing w/r/t other
		//commands more awkward, unless we set the offet to 0 (in which case,
		//noisy 'late' messages)
		super.prSendMsg(this.prUpdateStateMsg);
		super.prSendMsg(msg);
	}
}

FluidRealTimeModel : FluidModelObject
{
	*new{ |server, params|
		^super.new(server,params++[-1,-1]);
	}
}

FluidRTQuery : FluidProxyUgen
{
	*kr{ |trig,obj...args|
		^super.kr(this.name,trig,obj.asUGenInput, *args)
	}
}


FluidRTUGen : UGen
{
	*version{|server|
		server ?? {server = Server.default};
		server.sendMsg("/cmd","/"++this.name++'/version');
	}
}

FluidRTMultiOutUGen : MultiOutUGen
{
	*version{|server|
		server ?? {server = Server.default};
		server.sendMsg("/cmd","/"++this.name++'/version');
	}
}
