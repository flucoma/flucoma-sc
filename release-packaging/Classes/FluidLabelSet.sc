FluidLabelSet : FluidManipulationClient {

   var  <> synth, <> server, <>id;

    *kr{ |name|
        ^this.multiNew('control',name);
	}


    *new { |server,name|
        ^super.new(server,name);
    }

    init { |name|
        var ascii = name.ascii;
        this.id = name;
        inputs = [ascii.size].addAll(ascii)++Done.none++FluidManipulationClient.nonBlocking;
    }

    asString {
        ^id.asString;
    }

  
    *asUGenInput { |input|
      var ascii = input.asString.ascii;
      ^[ascii.size].addAll(ascii)
    }

    addLabel{|id, label, action|
        this.pr_sendMsg(\addLabel,[id.asString, label.asString],action);
    }

    getLabel{|id, action|
        this.pr_sendMsg(\getLabel,[id.asString],action,[string(FluidMessageResponse,_,_)]);
    }

    deleteLabel{|id, action|
        this.pr_sendMsg(\deleteLabel,[id.asString],action);
    }

    cols {|action|
        this.pr_sendMsg(\cols,[],action,[number(FluidMessageResponse,_,_)]);
    }

    read{|filename,action|
        this.pr_sendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.pr_sendMsg(\write,[filename.asString],action);
    }

    size { |action|
        this.pr_sendMsg(\size,[],action,[number(FluidMessageResponse,_,_)]);
    }

    clear { |action|
        this.pr_sendMsg(\clear,[],action);
    }
}   
