FluidLabelSet : FluidManipulationClient {

   var  <> synth, <> server, <>id;

    *kr{ |name|
        ^this.multiNew('control',name);
	}

    init { |name|
        var ascii = name.ascii;
        this.id = name;
        inputs = [ascii.size].addAll(ascii)++Done.none++FluidManipulationClient.nonBlocking;
    }

    asString {
        ^id.asString;
    }

    addLabel{|id, label, action|
        this.pr_sendMsg(\addLabel,[id, label],action);
    }

    getLabel{|id, action|
        this.pr_sendMsg(\getLabel,[id],action,[string(FluidMessageResponse,_,_)]);
    }

    deleteLabel{|id, action|
        this.pr_sendMsg(\deleteLabel,[id],action);
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
}   