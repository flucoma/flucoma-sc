FluidLabelSet : FluidManipulationClient {

   var  <> synth, <> server, id;

    *kr{ |name|
        ^this.multiNew('control',name);
	}

    init { |name|
        var ascii = name.ascii;
        this.id = name;
        inputs = [ascii.size].addAll(ascii)++Done.none++FluidManipulationClient.nonBlocking;
    }

    asString {
        ^id;
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
}   