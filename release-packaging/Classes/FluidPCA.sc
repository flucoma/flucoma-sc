FluidPCA : FluidManipulationClient {

    fit{|dataset, k, action|
        this.pr_sendMsg(\fit,[dataset.asString, k],action);
    }

    transform{|sourceDataset, destDataset, action|
        this.pr_sendMsg(\transform,[sourceDataset.asString, destDataset.asString],action);
    }

    fitTransform{|sourceDataset, k, destDataset, action|
        this.pr_sendMsg(\fitTransform,[sourceDataset.asString,k,  destDataset.asString],action);
    }


    transformPoint{|sourceBuffer, destBuffer, action|
        this.pr_sendMsg(\transformPoint,[sourceBuffer.asUGenInput, destBuffer.asUGenInput],action);
    }

    cols {|action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

	rows {|action|
        this.pr_sendMsg(\rows,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{|filename,action|
        this.pr_sendMsg(\read,[filename],action);
    }

    write{|filename,action|
        this.pr_sendMsg(\write,[filename],action);
    }

}   