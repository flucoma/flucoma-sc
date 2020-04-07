FluidStandardize : FluidManipulationClient {

    fit{|dataset, action|
        this.pr_sendMsg(\fit,[dataset.asString],action);
    }

    standardize{|sourceDataset, destDataset, action|
        this.pr_sendMsg(\standardize,[sourceDataset.asString, destDataset.asString],action);
    }

    standardizePoint{|sourceBuffer, destBuffer, action|
        this.pr_sendMsg(\standardizePoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.pr_sendMsg(\read,[filename.asString],action);
    }

    write{|filename,action|
        this.pr_sendMsg(\write,[filename.asString],action);
    }

}   