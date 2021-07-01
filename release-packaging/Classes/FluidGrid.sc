FluidGrid : FluidModelObject {
	var <>oversample, <>rows, <>cols;
	*new {|server, oversample = 1, rows = 0, cols = 0|
		^super.new(server,[oversample, rows, cols])
        .oversample_(oversample).rows_(rows).cols_(cols);
	}

    prGetParams{
        ^[this.oversample, this.rows, this.cols];
    }

	fitTransformMsg{|sourceDataSet, destDataSet|
        ^this.prMakeMsg(\fitTransform,id, sourceDataSet.id,  destDataSet.id);
    }

	fitTransform{|sourceDataSet, destDataSet, action|
        actions[\fitTransform] = [nil,action];
        this.fitTransformMsg(sourceDataSet,destDataSet);
		this.prSendMsg(this.fitTransformMsg(sourceDataSet,destDataSet));
	}

}
