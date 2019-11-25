FluidKMeans : UGen {

    var  <> synth, <> server;

	*kr {
        ^this.multiNew('control');
	}

    *new{ |server|
        var synth, instance;
        server = server ? Server.default;
        synth = {instance = FluidKMeans.kr()}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }

    train{|dataset,k, maxIter = 100, buffer, action|

       buffer = buffer ? -1;

       this.pr_sendMsg(\train,[dataset, k,maxIter, buffer.asUGenInput],action);
    }

    cluster{ |dataset, labelset, k, maxIter=100, buffer,action|
        buffer = buffer ? -1;
        this.pr_sendMsg(\cluster,[dataset, labelset, k, maxIter, buffer.asUGenInput],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    predict { |buffer, action|
        this.pr_sendMsg(\predict,[buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,1,_)]);
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
