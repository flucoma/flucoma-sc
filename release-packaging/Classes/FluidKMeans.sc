FluidKMeans : FluidRealTimeModel {

    var clusters, maxiter;

	*new {|server, numClusters = 4, maxIter = 100|
		^super.new(server,[numClusters,maxIter])
        .numClusters_(numClusters)
        .maxIter_(maxIter);
	}

    numClusters_{|n| clusters = n.asInteger}
    numClusters{ ^clusters }

    maxIter_{|i| maxiter = i.asInteger}
    maxIter{ ^maxiter }

    prGetParams{^[this.numClusters,this.maxIter,-1,-1];}

    fitMsg{ |dataSet| ^this.prMakeMsg(\fit,id,dataSet.id);}

    fit{|dataSet, action|
        actions[\fit] = [
            numbers( FluidMessageResponse, _, this.numClusters ,_),
            action
        ];
        this.prSendMsg(this.fitMsg(dataSet));
    }

    fitPredictMsg{|dataSet, labelSet|
        ^this.prMakeMsg(\fitPredict, id, dataSet.id, labelSet.id)
    }

	fitPredict{|dataSet, labelSet,action|
        actions[\fitPredict] = [
            numbers(FluidMessageResponse, _, this.numClusters, _),
            action
        ];
		this.prSendMsg(this.fitPredictMsg(dataSet,labelSet));
	}

    predictMsg{|dataSet, labelSet|
        ^this.prMakeMsg(\predict, id, dataSet.id, labelSet.id)
    }

	predict{ |dataSet, labelSet, action|
        actions[\predict] = [
            numbers(FluidMessageResponse, _, this.numClusters, _),
            action
        ];
		this.prSendMsg(this.predictMsg(dataSet,labelSet));
	}

    predictPointMsg{|buffer|
        ^this.prMakeMsg(\predictPoint, id, this.prEncodeBuffer(buffer))
    }

	predictPoint { |buffer, action|
		actions[\predictPoint] = [number(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(buffer))
	}

    kr{|trig, inputBuffer,outputBuffer|
        ^FluidKMeansQuery.kr(K2A.ar(trig),
                this, clusters, maxiter,
                this.prEncodeBuffer(inputBuffer),
                this.prEncodeBuffer(outputBuffer));
    }
}

FluidKMeansQuery : FluidRTQuery {}
