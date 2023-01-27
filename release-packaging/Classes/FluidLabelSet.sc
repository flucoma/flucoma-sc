FluidLabelSet : FluidDataObject {

	*new{|server| ^super.new(server) }

	addLabelMsg{|identifier,label|
		^this.prMakeMsg(\addLabel,id,identifier.asSymbol,label.asSymbol);
	}

	addLabel{|identifier, label, action|
		actions[\addLabel] = [nil, action];
		this.prSendMsg(this.addLabelMsg(identifier,label));
	}

	updateLabelMsg{|identifier, label|
		^this.prMakeMsg(\updateLabel, id, identifier.asSymbol, label.asSymbol);
	}

	setLabelMsg{|identifier,label|
		^this.prMakeMsg(\setLabel,id,identifier.asSymbol,label.asSymbol);
	}

	setLabel{|identifier, label, action|
		actions[\setLabel] = [nil, action];
		this.prSendMsg(this.setLabelMsg(identifier,label));
	}

	updateLabel{|identifier, label, action|
		actions[\updateLabel] = [nil,action];
		this.prSendMsg(this.updateLabelMsg(identifier,label));
	}

	getLabelMsg{|identifier|
		^this.prMakeMsg(\getLabel, id, identifier.asSymbol);
	}

	getLabel{|identifier, action|
		actions[\getLabel] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.getLabelMsg(identifier));
	}

	deleteLabelMsg{|identifier, action|
		^this.prMakeMsg(\deleteLabel, id, identifier.asSymbol);
	}

	deleteLabel{|identifier, action|
		actions[\deleteLabel] = [nil, action];
		this.prSendMsg(this.deleteLabelMsg(identifier));
	}

	clearMsg { ^this.prMakeMsg(\clear,id); }

	clear { |action|
		actions[\clear] = [nil,action];
		this.prSendMsg(this.clearMsg);
	}

	mergeMsg{|sourceLabelSet, overwrite = 0|
		^this.prMakeMsg(\merge,id,sourceLabelSet.asUGenInput,overwrite);
	}

	merge{|sourceLabelSet, overwrite = 0, action|
		actions[\merge] = [nil,action];
		this.prSendMsg(this.mergeMsg(sourceLabelSet,overwrite));
	}

	printMsg { ^this.prMakeMsg(\print,id); }

	print { |action=(postResponse)|
		actions[\print] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.printMsg);
	}

	getIdsMsg{|labelSet|
		^this.prMakeMsg(\getIds, id, labelSet.asUGenInput);
	}

	getIds{|labelSet, action|
		actions[\getIds] = [nil,action];
		this.prSendMsg(this.getIdsMsg(labelSet));
	}
}
