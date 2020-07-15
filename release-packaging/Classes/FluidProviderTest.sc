FluidProviderTest : UGen {

/*    var <> server;
    var <> nodeID;

    *new{ |server, name|



    }*/

    *kr{ |name,vals|
        ^this.new1('control',name,vals);
	}

    *new1 { |rate, name,vals|
        var ascii = name.ascii;
		var args;
		vals ?? {vals = []};
		if(vals.isArray.not) {vals = [vals]};
		args = ([rate, ascii.size].addAll(ascii) ++ vals.size).addAll(vals).addAll([1,1]);
		args.postln;
		^super.new1(*args);
    }

/*    init { |size...chars|
        specialIndex = -1;
        inputs = [size].addAll(chars);
    }*/

    addPoint{|server, nodeID, args, action|
        this.prSendMsg(server, nodeID, 'addPoint',args,action);
    }

    updatePoint{|server, nodeID, args, action|
        this.prSendMsg(server, nodeID, 'updatePoint',args,action);
    }

    deletePoint{|server, nodeID, args, action|
        this.prSendMsg(server,nodeID,  'deletePoint',args,action);
    }

    prSendMsg { |server, nodeID, msg, args, action,parser|

        server = server ? Server.default;

        server.listSendMsg(['/u_cmd',nodeID.nodeID,this.synthIndex,msg].addAll(args));

        OSCFunc(
            { |msg|
                var result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
                if(action.notNil){action.value(result)}{action.value};
        },'/'++msg).oneShot;
    }




}   