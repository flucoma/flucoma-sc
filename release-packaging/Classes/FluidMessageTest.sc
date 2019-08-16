FluidMessageTest : UGen {

    var server;

    *kr{ |doneAction = 0|

        ^this.multiNew('control', doneAction);
    }

    testReturnStrings { |server, nodeID, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnStrings');

        OSCFunc(
            { |msg|
                var resp =FluidMessageResponse.collectArgs(
                4.collect{string(FluidMessageResponse,_,_)}, msg.drop(3));
                if(action.notNil){action.value(resp);};
        },'/testReturnStrings').oneShot;

    }

    testReturnNumbers{ |server, nodeID, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnNumbers');

        OSCFunc(
        { |msg|
                var result = FluidMessageResponse.collectArgs(
                    [numbers(FluidMessageResponse,_,100,_)], msg.drop(3));
                if(action.notNil){action.value(result);};
        },'/testReturnNumbers').oneShot;
    }

    testReturnOneString{ |server, nodeID, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnOneString');

        OSCFunc(
        { |msg|
                var result = FluidMessageResponse.collectArgs(
                    [string(FluidMessageResponse,_,_)], msg.drop(3));
                if(action.notNil){action.value(result);};
        },'/testReturnOneString').oneShot;
    }

    testReturnOneNumber{ |server, nodeID, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnOneNumber');

        OSCFunc(
        { |msg|
                var result = msg.drop(3);
                if(action.notNil){action.value(result);};
        },'/testReturnOneNumber').oneShot;
    }

    testAccessBuffer{ |server, nodeID, buf, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testAccessBuffer', buf.asUGenInput);

        OSCFunc(
        { |msg|
                var result = FluidMessageResponse.collectArgs([numbers(FluidMessageResponse,_,1,_)],msg.drop(3));
                if(action.notNil){action.value(result);};
        },'/testAccessBuffer').oneShot;
    }

    testPassString{ |server, nodeID, str, a, b, c, d, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testPassString', str, a, b, c);

        OSCFunc(
        { |msg|
                if(action.notNil){action.value;};
        },'/testPassString').oneShot;
    }


    testReturnBuffer{ |server, nodeID, b, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnBuffer', b.asUGenInput);

        OSCFunc(
        { |msg|
                var result = result = FluidMessageResponse.collectArgs([buffer(FluidMessageResponse,_,server,_)],msg.drop(3));
                if(action.notNil){action.value(result);};
        },'/testReturnBuffer').oneShot;
    }

    testReturnHetero{ |server, nodeID, action|

        server = server ? Server.default;

        server.sendMsg('/u_cmd',nodeID,this.synthIndex,'testReturnHetero');

        OSCFunc(
        { |msg|
                var result = result = FluidMessageResponse.collectArgs([string(FluidMessageResponse,_,_), numbers(FluidMessageResponse,_,2,_)],msg.drop(3));
                if(action.notNil){action.value(result);};
        },'/testReturnHetero').oneShot;
    }



}