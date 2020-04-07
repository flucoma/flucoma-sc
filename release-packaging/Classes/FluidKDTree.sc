FluidKDTree : FluidManipulationClient {

    fit{|dataset,action|
       this.pr_sendMsg(\fit,[dataset.asString],action);
    }

    kNearest{ |buffer, k,action|
        this.pr_sendMsg(\kNearest,[buffer.asUGenInput,k],action,k.collect{string(FluidMessageResponse,_,_)});
    }

    kNearestDist { |buffer, k,action|
        this.pr_sendMsg(\kNearestDist,[buffer.asUGenInput,k],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    cols { |action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{ |filename,action|
        this.pr_sendMsg(\read,[filename.asString],action);
    }

    write{ |filename,action|
        this.pr_sendMsg(\write,[filename.asString],action);
    }

}
