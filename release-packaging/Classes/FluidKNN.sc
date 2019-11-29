FluidKNN : FluidManipulationClient {

    index{|dataset, action|
       this.pr_sendMsg(\index,[dataset.asString],action);
    }

    classify{ |buffer, labelset, k, action|
        this.pr_sendMsg(\classify,[buffer.asUGenInput, labelset.asString, k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    regress { |buffer,dataset, k, action|
        this.pr_sendMsg(\regress,[buffer.asUGenInput, dataset.asString,k],action,[number(FluidMessageResponse,_,_)]);
    }

}
