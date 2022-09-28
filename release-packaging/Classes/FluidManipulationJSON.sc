+ FluidDataObject {
	tmpJSONFilename{
		^Platform.defaultTempDir++"tmp_fluid_data_"++
		Date.localtime.stamp++"_"++UniqueID.next++".json";
	}

	dump {|action|
		var filename = this.tmpJSONFilename;
		action ?? {action = postResponse};
		this.write(filename, {
			action.value(this.parseJSON(File.readAllString(filename)));
			File.delete(filename);
		});
	}

	load{|dict, action|
		var filename = this.tmpJSONFilename;
		File.use(filename, "wt", { |f| f.write(this.asJSON(dict));});
		this.read(filename, {
			action.notNil.if{ action.value; };
			File.delete(filename);
		});
	}

	toDict{|obj|
		var converted;
		if(obj.class === Event){
			converted = obj.as(Dictionary);
			converted.keysValuesChange{|k,v|this.toDict(v)}
			^converted;
		};
		if(obj.class === Array){
			converted = obj.collect{|v| this.toDict(v)};
			^converted;
		};
		^obj;
	}

	parseJSON{|jsonStr|
		var parsed = jsonStr;
		jsonStr.do({|char,pos|
			var inString = false;
			char.switch(
				$",{(jsonStr[pos-1]==$\ && inString).not.if({inString = inString.not})},
				${,{ if(inString.not){parsed[pos] = $(} },
				$},{ if(inString.not){parsed[pos] = $)} }
			)
		});
		^this.toDict(parsed.interpret);
	}

	asJSON{|d|
		if(d.isNumber){^d};
		if(d.isString){^d.asString.asCompileString};
		if(d.isKindOf(Symbol)){^this.asJSON(d.asString)};
		if(d.isKindOf(Dictionary))
		{
			^"{" ++ (
				d.keys.asList.collect{|k|
					k.asString.asCompileString ++ ":" + this.asJSON(d[k])
			}).join(", ") ++ "}"
		};
		if(d.isKindOf(SequenceableCollection))
		{
			^"[" ++ d.collect({|x|this.asJSON(x)}).join(", ")++ "]";
		};
	}
}
