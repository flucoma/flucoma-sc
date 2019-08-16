FluidMessageResponse : Object
{
    //selectors is an array of functions
    //my cunning thought is that those that need extra data (e..g numbers()) can
    //use partial applicaiton
    *collectArgs{ |selectors,a|
        var response = [];
        var idx = 0;
        selectors.do{ |selector|
            var newThings;
            # newThings,idx = selector.value(a, idx);
            response = response.add(newThings);
        };
        ^response
    }

    *string{ |a, offset|
        var split = a.find([0],offset);
        var res;
        if(split.isNil) {"ERROR: can't parse string from server".throw};
        ^[a.copyRange(offset,split-1).keep(split).collectAs({|x|x.asInt.asAscii},String), split + 1]
    }

    *numbers{ |a, n, offset|
        ^[a.copyRange(offset, offset + n),offset + n]
    }

    *buffer{ |a,server,offset|
        server = server ? Server.default ;
        ^[Buffer.cachedBufferAt(server, a[offset]), offset + 1]
    }
}