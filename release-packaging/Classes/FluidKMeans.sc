FluidKMeans : FluidManipulationClient {

    var <>k;

    fit{|dataset,k, maxIter = 100, buffer, action|
       buffer = buffer ? -1;
        this.k = k;
        this.pr_sendMsg(\fit,[dataset.asString, k,maxIter, buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,k,_)]);
    }

    predict{ |dataset, labelset,action|
        this.pr_sendMsg(\predict,[dataset.asString, labelset.asString],action,[numbers(FluidMessageResponse,_,this.k,_)]);
    }

    getClusters{ |dataset, labelset,action|
        this.pr_sendMsg(\getClusters,[dataset.asString, labelset.asString],action);
    }

    predictPoint { |buffer, action|
        this.pr_sendMsg(\predictPoint,[buffer.asUGenInput],action,[number(FluidMessageResponse,_,_)]);
    }

    cols { |action|
        this.pr_sendMsg(\cols,[],action,[number(FluidMessageResponse,_,_)]);
    }

    read{ |filename,action|
        this.pr_sendMsg(\read,[filename],action);
    }

    write{ |filename,action|
        this.pr_sendMsg(\write,[filename],action);
    }
}
