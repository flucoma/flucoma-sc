FluidKNNRegressor : FluidManipulationClient {
    fit{|sourceDataset, targetDataset, action|
       this.pr_sendMsg(\fit,
			[sourceDataset.asString, targetDataset.asString],
			action
		);
    }

    predict{ |sourceDataset, targetDataset, k, action|
        this.pr_sendMsg(\predict,
			[sourceDataset.asString, targetDataset.asString, k],
			action,
			[string(FluidMessageResponse,_,_)]);
    }

    predictPoint { |buffer, k, action|
        this.pr_sendMsg(\predictPoint, [buffer.asUGenInput, k], action,
			[number(FluidMessageResponse,_,_)]);
    }
}
