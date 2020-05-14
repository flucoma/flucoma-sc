FluidKNNClassifier : FluidManipulationClient {
    fit{|dataset, labelset, action|
       this.pr_sendMsg(\fit,[dataset.asString, labelset.asString], action);
    }

    predict{ |dataset, labelset, k, action|
        this.pr_sendMsg(\predict,
			[dataset.asString, labelset.asString, k],
			action, [string(FluidMessageResponse,_,_)]
		);
    }

    predictPoint { |buffer, k, action|
        this.pr_sendMsg(\predictPoint,
			[buffer.asUGenInput, k], action,
			[number(FluidMessageResponse,_,_)]
		);
	}
}