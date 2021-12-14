FluidFilesPath {
	*new {
		arg fileName;
		fileName = fileName ? "";
		^("%/../AudioFiles/".format(File.realpath(FluidDataSet.class.filenameSymbol).dirname) +/+ fileName);
	}
}