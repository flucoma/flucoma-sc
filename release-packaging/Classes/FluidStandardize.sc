FluidStandardize : FluidManipulationClient {

    fit{|dataset, action|
        this.pr_sendMsg(\fit,[dataset.asUGenInput],action);
    }

    standardize{|sourceDataset, destDataset, action|
        this.pr_sendMsg(\standardize,[sourceDataset.asUGenInput, destDataset.asUGenInput],action);
    }

    standardizePoint{|sourceBuffer, destBuffer, action|
        this.pr_sendMsg(\standardizePoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
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