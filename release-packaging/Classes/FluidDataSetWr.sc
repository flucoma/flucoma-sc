FluidDataSetWr : FluidBufProcessor {
	*kr  { |dataset,labelPrefix = "", labelOffset = 0,buf, trig=1, blocking = 1|
        var args;
        buf ?? {"No input buffer provided".error};
        labelPrefix = labelPrefix !? {[labelPrefix.asString.size] ++ labelPrefix.asString.ascii} ?? {0};

        args = [-1] ++ dataset.asUGenInput ++labelPrefix ++ labelOffset.asInteger.asUGenInput ++buf.asUGenInput ++  trig ++ blocking;

        ^FluidProxyUgen.kr(\FluidDataSetWrTrigger,*args);
	}
}

FluidDataSetWrTrigger : FluidProxyUgen {} 
