FluidGrid : FluidModelObject {
	var <>oversample, <>extent, <>axis;
	*new {|server, oversample = 1, extent = 0, axis = 0|
		^super.new(server,[oversample, extent, axis])
		.oversample_(oversample).extent_(extent).axis_(axis);
	}

	prGetParams{
		^[this.oversample, this.extent, this.axis];
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
