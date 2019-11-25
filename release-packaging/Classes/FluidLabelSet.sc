FluidLabelSet : UGen {

   var  <> synth, <> server;

    *kr{ |name|
        ^this.multiNew('control',name);
	}

    *new{ |server, name|
        var synth, instance;
        server = server ? Server.default;
        synth = {instance = FluidLabelSet.kr(name)}.play(server);
        instance.server = server;
        instance.synth = synth;
        ^instance
    }

    *new1 { |rate, name|
        var ascii = name.ascii;
        ^super.new1(*[rate, ascii.size].addAll(ascii));
    }

    init { |size...chars|
        //Send the number of inputs (size of provider string) as specialIndex,
        //so server plugin knows what's going on
        specialIndex = -1;
        inputs = [size].addAll(chars);
    }

    addPoint{|id, label, action|
        this.pr_sendMsg(\addPoint,[id, label],action);
    }

    getPoint{|id, action|
        this.pr_sendMsg(\getPoint,[id],action);
    }

    updatePoint{|id, label, action|
        this.pr_sendMsg(\updatePoint,[id, label],action);
    }

    deletePoint{|id, action|
        this.pr_sendMsg(\deletePoint,[id],action);
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

    size { |action|
        this.pr_sendMsg(\size,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    clear { |action|
        this.pr_sendMsg(\clear,[],action);
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