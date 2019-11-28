FluidKNN : FluidManipulationClient {

	*kr { |dims,k|
        ^this.multiNew('control', dims, k, Done.none, super.nonBlocking);
	}

    index{|dataset, action|
       this.pr_sendMsg(\index,[dataset.asUGenInput],action);
    }

    classify{ |buffer, labelset, k, action|
        this.pr_sendMsg(\classify,[buffer.asUGenInput, labelset.asUGenInput, k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    regress { |buffer,dataset, k, action|
        this.pr_sendMsg(\regress,[buffer.asUGenInput, dataset.asUGenInput,k],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

}
