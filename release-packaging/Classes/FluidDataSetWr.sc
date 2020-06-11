FluidDataSetWr : UGen {

	*new1 { |rate, dataset, labelPrefix = "", labelOffset = 0, buf, trig, blocking|
		buf ?? {"No input buffer provided".error};
		^super.new1(rate,*(FluidManipulationClient.prServerString(dataset.asSymbol)
			++ FluidDataSet.asUGenInput(labelPrefix.asSymbol) ++ labelOffset.asInteger.asUGenInput ++buf.asUGenInput ++  trig ++ blocking));
	}

	*kr { |dataset,labelPrefix = "", labelOffset = 0,buf, trig=1|
		^this.new1(\control,dataset,labelPrefix,labelOffset, buf, trig,1)
	}
}
