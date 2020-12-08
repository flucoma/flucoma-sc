FluidMLPRegressor : FluidRealTimeModel {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

    var <>hidden, <>activation, <>outputActivation, <>tapIn, <>tapOut, <>maxIter, <>learnRate, <>momentum, <>batchSize, <>validation;

	*new {|server, hidden = #[3,3] , activation = 2, outputActivation = 0, tapIn = 0, tapOut = -1,maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|

		^super.new(server, [hidden.size] ++ hidden ++ [activation, outputActivation, tapIn, tapOut, maxIter, learnRate, momentum, batchSize, validation])
            .hidden_(hidden)
            .activation_(activation)
            .outputActivation_(outputActivation)
            .tapIn_(tapIn)
            .tapOut_(tapOut)
            .maxIter_(maxIter)
            .learnRate_(learnRate)
            .momentum_(momentum)
            .batchSize_(batchSize)
            .validation_(validation);
	}

    prGetParams{
        ^[this.hidden.size] ++ this.hidden ++ [this.activation, this.outputActivation, this.tapIn, this.tapOut, this.maxIter, this.learnRate, this.momentum, this.batchSize, this.validation, -1, -1]
    }

    clearMsg{ ^this.prMakeMsg(\clear, id) }

    clear{ |action|
        actions[\clear] = [nil, action];
		this.prSendMsg(this.clearMsg);
	}

    fitMsg{|sourceDataSet, targetDataSet|
        ^this.prMakeMsg(\fit,id,sourceDataSet.id, targetDataSet.id);
    }

	fit{|sourceDataSet, targetDataSet, action|
	   actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
       this.prSendMsg(this.fitMsg(sourceDataSet,targetDataSet));
	}

    predictMsg{|sourceDataSet, targetDataSet|
        ^this.prMakeMsg(\predict,id,sourceDataSet.id, targetDataSet.id);
    }

	predict{|sourceDataSet, targetDataSet, action|
	   actions[\predict] = [nil,action];
       this.prSendMsg(this.predictMsg(sourceDataSet,targetDataSet));
	}


	predictPointMsg { |sourceBuffer, targetBuffer|
        ^this.prMakeMsg(\predictPoint,id,
            this.prEncodeBuffer(sourceBuffer),
            this.prEncodeBuffer(targetBuffer),
            ["/b_query", targetBuffer.asUGenInput]);
    }

	predictPoint { |sourceBuffer, targetBuffer, action|
        actions[\predictPoint] = [nil,{action.value(targetBuffer)}];
        this.predictPointMsg(sourceBuffer, targetBuffer).postln;
		this.prSendMsg(this.predictPointMsg(sourceBuffer, targetBuffer));
	}

    kr{|trig, inputBuffer,outputBuffer, tapIn = 0, tapOut = -1|
        var params;
        tapIn = tapIn ? this.tapIn;
        tapOut = tapOut ? this.tapOut;

        this.tapIn_(tapIn).tapOut_(tapOut);

        params = this.prGetParams.drop(-2) ++ [this.prEncodeBuffer(inputBuffer),
        this.prEncodeBuffer(outputBuffer)];

        ^FluidProxyUgen.kr(this.class.name.asString++'/query', K2A.ar(trig),
                id, *params);
    }

}


FluidMLPClassifier : FluidRealTimeModel {

	const <identity = 0;
	const <sigmoid  =  1;
	const <relu = 2;
	const <tanh = 3;

    var <>hidden, <>activation, <> maxIter, <>learnRate, <> momentum, <>batchSize, <>validation;

	*new {|server, hidden = #[3,3] , activation = 2, maxIter = 1000, learnRate = 0.0001, momentum = 0.9, batchSize = 50, validation = 0.2|
		^super.new(server,[hidden.size] ++ hidden ++ [activation,  maxIter, learnRate,  momentum, batchSize, validation])
        .hidden_(hidden)
			.activation_(activation)
			.maxIter_(maxIter)
			.learnRate_(learnRate)
			.momentum_(momentum)
			.batchSize_(batchSize)
			.validation_(validation);
	}

    prGetParams{
        ^[ this.hidden.size] ++ this.hidden ++ [this.activation,  this.maxIter, this.learnRate,  this.momentum, this.batchSize, this.validation, -1, -1];
    }

    clearMsg{ ^this.prMakeMsg(\clear,id) }

	clear{ |action|
        actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

    fitMsg{|sourceDataSet, targetLabelSet|
        ^this.prMakeMsg(\fit,id,sourceDataSet.id, targetLabelSet.id);
    }

	fit{|sourceDataSet, targetLabelSet, action|
       actions[\fit] = [numbers(FluidMessageResponse,_,1,_),action];
	   this.prSendMsg(this.fitMsg(sourceDataSet,targetLabelSet));
	}

    predictMsg{|sourceDataSet, targetLabelSet|
        ^this.prMakeMsg(\predict,id,sourceDataSet.id, targetLabelSet.id);
    }

	predict{ |sourceDataSet, targetLabelSet, action|
        actions[\predict]=[nil,action];
		this.prSendMsg(this.predictMsg(sourceDataSet,targetLabelSet));
	}

    predictPointMsg { |sourceBuffer|
        ^this.prMakeMsg(\predictPoint,id,this.prEncodeBuffer(sourceBuffer))
    }

	predictPoint { |sourceBuffer, action|
        actions[\predictPoint] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.predictPointMsg(sourceBuffer));
	}

    kr{|trig, inputBuffer,outputBuffer|

        var params = this.prGetParams.drop(-2) ++  [this.prEncodeBuffer(inputBuffer),
        this.prEncodeBuffer(outputBuffer)];

        ^FluidProxyUgen.kr(this.class.name.asString++'/query', K2A.ar(trig),
                id, *params);
    }
}
