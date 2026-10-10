FluidFilesPath {

	classvar resourcePath;

	*new {
		arg fileName;

		resourcePath = resourcePath ? Main.packages.collect{|a|
			(a.value +/+ "Resources/AudioFiles/Tremblay*").pathMatch
		}.flatten[0].dirname;

		fileName = fileName ? "";
		^(resourcePath +/+ fileName);
	}
}
