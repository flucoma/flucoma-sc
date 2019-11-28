FluidKMeans : FluidManipulationClient {

 /*   var  <> synth, <> server,k;

	*kr {
        ^this.multiNew('control',Done.none);
	}
*/
/*    *new{ |server|
        var synth, instance;
        server = server ? Server.default;
        this.asString.postln;
        if(server.serverRunning.not,{"ERROR: FluidKMeans – server not running".postln; ^nil});
        synth = {instance = FluidKMeans.kr()}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }*/

    fit{|dataset,k, maxIter = 100, buffer, action|
       buffer = buffer ? -1;
        this.k = k;
        this.pr_sendMsg(\fit,[dataset.asString, k,maxIter, buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    predict{ |dataset, labelset,action|
        this.pr_sendMsg(\predict,[dataset.asString, labelset.asString],action,[numbers(FluidMessageResponse,_,this.k,_)]);
    }

    predictPoint { |buffer, action|
        this.pr_sendMsg(\predictPoint,[buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,1,_)]);
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
