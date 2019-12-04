FluidKNN : FluidManipulationClient {

    fit{|dataset, action|
       this.pr_sendMsg(\fit,[dataset.asString],action);
    }

    classifyPoint{ |buffer, labelset, k, action|
        this.pr_sendMsg(\classify,[buffer.asUGenInput, labelset.asString, k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    regressPoint { |buffer,dataset, k, action|
        this.pr_sendMsg(\regress,[buffer.asUGenInput, dataset.asString,k],action,[number(FluidMessageResponse,_,_)]);
    }

}
