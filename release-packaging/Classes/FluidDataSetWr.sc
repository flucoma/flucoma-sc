FluidDataSetWr : FluidBufProcessor {
	*kr  { |dataset,idPrefix = "", idNumber = 0,buf, trig=1, blocking = 1|
		var args;
		buf ?? {(this.class.name ++ ": No input buffer provided").error};

		idNumber = idNumber !? {[2,1,idNumber.asInteger.asUGenInput]} ?? {[2,0,0]};
		idPrefix = idPrefix !? {[idPrefix.asString.size] ++ idPrefix.asString.ascii} ?? {0};

		args = [-1] ++ dataset.asUGenInput ++idPrefix ++ idNumber ++ buf.asUGenInput ++  trig ++ blocking;

		^FluidProxyUgen.kr(\FluidDataSetWrTrigger,*args);
	}
}

FluidDataSetWrTrigger : FluidProxyUgen {}
