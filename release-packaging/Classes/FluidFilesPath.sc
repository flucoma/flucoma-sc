FluidFilesPath {
	*new {
		arg fileName;
		fileName = fileName ? "";
		^("%/../Resources/AudioFiles/".format(File.realpath(FluidDataSet.class.filenameSymbol).dirname) +/+ fileName);
	}
}