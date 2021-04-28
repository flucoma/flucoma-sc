FluidSubscriberTest : FluidModelObject {

    var <> providerName;
    var <> nodeID;

    // *kr { |provider|
    //     ^this.multiNew('control',provider);
	// }
    // 
    // *new1 { |rate, provider|
    //     var ascii = provider.ascii;
    //     ^super.new1(*[rate, ascii.size].addAll(ascii));
    // }
    // 
    // init { |size...chars|
    //     //Send the number of inputs (size of provider string) as specialIndex,
    //     //so server plugin knows what's going on
    //     specialIndex = -1;
    //     inputs = [size].addAll(chars);
    //     providerName = chars.collectAs({|x|x.asInteger.asAscii}, String);
    // }

    providerLookup { |server, nodeID, label, action|
        this.prSendMsg(server, nodeID, 'providerLookup', label, action,
            [string(FluidMessageResponse,_,_),numbers(FluidMessageResponse,_,2,_)] );
    }

    prSendMsg { |server, nodeID, msg, args, action,parser|

        server = server ? Server.default;

        server.listSendMsg(['/u_cmd',nodeID.nodeID,this.synthIndex,msg].addAll(args));

        OSCFunc(
            { |msg|
                var result = FluidMessageResponse.collectArgs(parser,msg.drop(3));
                if(action.notNil){action.value(result)}{action.value};
        },'/'++msg).oneShot;
    }
}
