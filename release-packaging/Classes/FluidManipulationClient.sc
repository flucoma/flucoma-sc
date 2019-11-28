FluidManipulationClient : UGen {

    const < nonBlocking = 0;
    var  <> synth, <> server;

    *kr {
        ^this.multiNew('control', Done.none, nonBlocking);
    }

    *new{ |server...args|
        var synth, instance;
        server = server ? Server.default;
        if(server.serverRunning.not,{("ERROR:" + this.asString + "– server not running").postln; ^nil});
        synth = {instance = this.kr(*args)}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
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
