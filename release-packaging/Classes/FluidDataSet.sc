FluidDataSet : FluidManipulationClient {

    var <>synth, <>server, <>id;

    *kr{ |name|
        ^this.new1('control',name);
	}

    *new { |server,name|
        ^super.new(server,name);
    }

    init { |name, dims|
        var ascii = name.ascii;
        this.id = name;
        // specialIndex = -1;
        inputs = [ascii.size].addAll(ascii)++dims++Done.none++FluidManipulationClient.nonBlocking;
    }

    asString {
        ^id.asString;
    }

    addPoint{|label, buffer, action|
        this.pr_sendMsg(\addPoint,[label,buffer.asUGenInput],action);
    }

    getPoint{|label, buffer, action|
        this.pr_sendMsg(\getPoint,[label,buffer.asUGenInput],action);
    }

    updatePoint{|label, buffer, action|
        this.pr_sendMsg(\updatePoint,[label,buffer.asUGenInput],action);
    }

    deletePoint{|label, action|
        this.pr_sendMsg(\deletePoint,[label],action);
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