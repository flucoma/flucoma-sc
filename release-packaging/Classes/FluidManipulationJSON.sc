+ FluidManipulationClient {
	tmpJSONFilename{
		^Platform.defaultTempDir++"tmp_fluid_dataset_"++
		Date.localtime.stamp++".json";
	}

	dump {|action|
		var filename = this.tmpJSONFilename;
		action ?? {action = postit};
		this.write(filename, {
			action.value(filename.parseYAMLFile);
			File.delete(filename);
		});
	}

	load{|dict, action|
		var filename = this.tmpJSONFilename;
		var str = this.asJSON(dict);
		File.use(filename, "w", { |f| f.write(this.asJSON(dict));});
		this.read(filename, {
			action.notNil.if{ action.value };
			File.delete(filename);
		});
	}

	asJSON{|d|
		if(d.isString || d.isNumber){^d};
		if(d.isKindOf(Dictionary),
		{
		  ^"{" ++ (
			d.keys.asList.collect{|k|
			k.asString.asCompileString ++ ":" + this.asJSON(d[k])
			}).join(", ") ++ "}"
		});
		if(d.isKindOf(SequenceableCollection),
		{
			^"[" ++ d.collect({|x|this.asJSON(x)}).join(", ")++ "]";
		});
	}
}
