FluidKNN : UGen {

    var  <> synth, <> server;

	*kr { |dims,k|
        ^this.multiNew('control');
	}

    *new{ |server,dims,k|
        var synth, instance;
        server = server ? Server.default;
        synth = {instance = FluidKNN.kr(dims,k)}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }

    index{|dataset, action|
       this.pr_sendMsg(\index,[dataset],action);
    }

    classify{ |buffer, labelset, k, action|
        this.pr_sendMsg(\classify,[buffer.asUGenInput, labelset, k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    regress { |buffer,dataset, k, action|
        this.pr_sendMsg(\regress,[buffer.asUGenInput, dataset,k],action,[numbers(FluidMessageResponse,_,1,_)]);
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
