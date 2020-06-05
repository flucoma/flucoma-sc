FluidDataSetWr : UGen {

	*new1 { |rate,label,buf,dataset,trig,blocking|
		buf ?? {"No input buffer provided".error};
		^super.new1(rate,*(FluidManipulationClient.prServerString(label.asSymbol)
			++buf.asUGenInput++FluidDataSet.asUGenInput(dataset.asSymbol)++trig++blocking));
	}

	*kr { |label, buf, dataset,trig=1|
		^this.new1(\control,label,buf,dataset,trig,1)
	}
}
