FluidProviderTest : UGen {

/*    var <> server;
    var <> nodeID;

    *new{ |server, name|



    }*/

    *kr{ |name|
        ^this.multiNew('control',name);
	}

    *new1 { |rate, name|
        var ascii = name.ascii;
        ^super.new1(*[rate, ascii.size].addAll(ascii));
    }

    init { |size...chars|
        //Send the number of inputs (size of provider string) as specialIndex,
        //so server plugin knows what's going onnode
        specialIndex = -1;
        inputs = [size].addAll(chars);
    }

    addPoint{|server, nodeID, args, action|
        this.pr_sendMsg(server, nodeID, 'addPoint',args,action);
    }

    updatePoint{|server, nodeID, args, action|
        this.pr_sendMsg(server, nodeID, 'updatePoint',args,action);
    }

    deletePoint{|server, nodeID, args, action|
        this.pr_sendMsg(server,nodeID,  'deletePoint',args,action);
    }

    pr_sendMsg { |server, nodeID, msg, args, action,parser|

        server = server ? Server.default;

        server.listSendMsg(['/u_cmd',nodeID.nodeID,this.synthIndex,msg].addAll(args));

        OSCFunc(
            { |msg|
                var result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
                if(action.notNil){action.value(result)}{action.value};
        },'/'++msg).oneShot;
    }




}   