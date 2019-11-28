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

    predictPoint { |buffer, action|
        this.pr_sendMsg(\predictPoint,[buffer.asUGenInput],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    cols { |action|
        this.pr_sendMsg(\cols,[],action,[numbers(FluidMessageResponse,_,1,_)]);
    }

    read{ |filename,action|
        this.pr_sendMsg(\read,[filename],action);
    }

    write{ |filename,action|
        this.pr_sendMsg(\write,[filename],action);
    }
}
