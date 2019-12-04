FluidNormalize : FluidManipulationClient {

    *kr{ |min = 0, max = 1|
        ^this.multiNew('control',min, max, Done.none, super.nonBlocking);
	}

    *new { |server,min = 0 ,max = 1|
        ^super.new(server,min,max);
    }

    fit{|dataset, action|
        this.pr_sendMsg(\fit,[dataset.asUGenInput],action);
    }

    normalize{|sourceDataset, destDataset, action|
        this.pr_sendMsg(\normalize,[sourceDataset.asUGenInput, destDataset.asUGenInput],action);
    }

    normalizePoint{|sourceBuffer, destBuffer, action|
        this.pr_sendMsg(\normalizePoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
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

}   