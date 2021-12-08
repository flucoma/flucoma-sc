FluidFilesPath {
	*new {
		^"%/../AudioFiles/".format(File.realpath(FluidDataSet.class.filenameSymbol).dirname);
	}
}