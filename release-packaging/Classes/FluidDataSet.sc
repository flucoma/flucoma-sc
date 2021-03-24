
FluidDataSet : FluidDataObject
{
    *new{|server| ^super.new(server) }

    addPointMsg{|label,buffer|
        buffer = this.prEncodeBuffer(buffer);
        ^this.prMakeMsg(\addPoint,id,label.asSymbol,buffer);
    }

	addPoint{|label, buffer, action|
      actions[\addPoint] = [nil,action];
	  this.prSendMsg(this.addPointMsg(label,buffer));
	}

    getPointMsg{|label,buffer|
        buffer = this.prEncodeBuffer(buffer);
        ^this.prMakeMsg(\getPoint,id,label.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
    }

    getPoint{|label, buffer, action|
      actions[\getPoint] = [nil,action];
      this.prSendMsg(this.getPointMsg(label,buffer));
    }

    updatePointMsg{|label,buffer|
        buffer = this.prEncodeBuffer(buffer);
        ^this.prMakeMsg(\updatePoint,id,label.asSymbol,buffer,["/b_query",buffer.asUGenInput]);
    }

    updatePoint{|label, buffer, action|
      actions[\updatePoint] = [nil,action];
      this.prSendMsg(this.updatePointMsg(label,buffer));
    }

    deletePointMsg{|label| ^this.prMakeMsg(\deletePoint,id,label.asSymbol);}

    deletePoint{|label, buffer, action|
      actions[\deletePoint] = [nil,action];
      this.prSendMsg(this.deletePointMsg(label));
    }

    setPointMsg{|label,buffer|
        buffer = this.prEncodeBuffer(buffer);
        ^this.prMakeMsg(\setPoint,id,label.asSymbol,buffer);
    }

	setPoint{|label, buffer, action|
      actions[\setPoint] = [nil,action];
	  this.prSendMsg(this.setPointMsg(label,buffer));
	}

	clearMsg { ^this.prMakeMsg(\clear,id); }

    clear { |action|
      actions[\clear] = [nil,action];
	  this.prSendMsg(this.clearMsg);
	}

    mergeMsg{|sourceDataSet, overwrite = 0|
        ^this.prMakeMsg(\merge,id,sourceDataSet.asUGenInput,overwrite);
    }

	merge{|sourceDataSet, overwrite = 0, action|
        actions[\merge] = [nil,action];
		this.prSendMsg(this.mergeMsg(sourceDataSet,overwrite));
	}

    printMsg { ^this.prMakeMsg(\print,id); }

	print { |action=(postResponse)|
		actions[\print] = [string(FluidMessageResponse,_,_),action];
		this.prSendMsg(this.printMsg);
	}

	toBufferMsg{|buffer, transpose = 0, labelSet|
        buffer = this.prEncodeBuffer(buffer);
       ^this.prMakeMsg(\toBuffer, id, buffer, transpose, labelSet.asUGenInput);
    }

	toBuffer{|buffer, transpose = 0, labelSet, action|
      actions[\toBuffer] = [nil,action];
		this.prSendMsg(this.toBufferMsg(buffer, transpose, labelSet));
	}

	fromBufferMsg{|buffer, transpose = 0, labelSet|
        buffer = this.prEncodeBuffer(buffer);
       ^this.prMakeMsg(\fromBuffer, id, buffer, transpose, labelSet.asUGenInput);
    }

	fromBuffer{|buffer, transpose = 0, labelSet, action|
      actions[\fromBuffer] = [nil,action];
		this.prSendMsg(this.fromBufferMsg(buffer, transpose, labelSet));
	}

	getIdsMsg{|labelSet|
        ^this.prMakeMsg(\getIds, id, labelSet.asUGenInput);
    }

	getIds{|labelSet, action|
      actions[\getIds] = [nil,action];
	  this.prSendMsg(this.getIdsMsg(labelSet));
	}
}

