FluidNormalize : UGen {

   var  <> synth, <> server;

    *kr{ |min, max|
        ^this.multiNew('control',min, max);
	}

    *new{ |server, min, max|
        var synth, instance;
        server = server ? Server.default;
        synth = {instance = FluidNormalize.kr(min,max)}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }

    fit{|dataset, action|
        this.pr_sendMsg(\fit,[dataset],action);
    }

    normalize{|sourceDataset, destDataset, action|
        this.pr_sendMsg(\normalize,[sourceDataset, destDataset],action);
    }

    normalizePoint{|sourceBuffer, destBuffer, action|
        this.pr_sendMsg(\normalizePoint,[sourceBuffer, destBuffer],action);
    }

    cols {|action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.pr_sendMsg(\read,[filename],action);
    }

    write{|filename,action|
        this.pr_sendMsg(\write,[filename],action);
    }

    pr_sendMsg { |msg, args, action,parser|
        OSCFunc(
            { |msg|
                var result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
                if(result.notNil){action.value(result)}{action.value};
            },'/'++msg
        ).oneShot;

        this.server.listSendMsg(['/u_cmd',this.synth.nodeID,this.synthIndex,msg].addAll(args));
    }
}   