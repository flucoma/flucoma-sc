FluidKDTree : UGen {

    var  <> synth, <> server;

	*kr {
        ^this.multiNew('control');
	}

    *new{ |server|
        var synth, instance;
        server = server ? Server.default;
        synth = {instance = FluidKDTree.kr()}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }

    index{|dataset,action|
       this.pr_sendMsg(\index,[dataset],action);
    }

    kNearest{ |buffer, k,action|
        this.pr_sendMsg(\kNearest,[buffer.asUGenInput,k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    kNearestDist { |buffer, k,action|
        this.pr_sendMsg(\kNearestDist,[buffer.asUGenInput,k],action,[numbers(FluidMessageResponse,_,3,_)]);
    }

    cols { |action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{ |filename,action|
        this.pr_sendMsg(\read,[filename],action);
    }

    write{ |filename,action|
        this.pr_sendMsg(\write,[filename],action);
    }

    pr_sendMsg { |msg, args, action,parser|

        OSCFunc(
            { |msg|
                var result;
                // msg.postln;
                result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
                if(action.notNil){action.value(result)}{action.value};
        },'/'++msg).oneShot;

        this.server.listSendMsg(['/u_cmd',this.synth.nodeID,this.synthIndex,msg].addAll(args));
    }
}
