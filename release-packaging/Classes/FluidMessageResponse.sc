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
			response = response ++ newThings;
		};

		if(response.size == 1,
			{^response[0]},{^response})
	}

	*string{ |a, offset|
		^[a]
	}

	*strings {|a,offset|
		//TODO add an n argument as with numbers() to make this less omnivorous
		^[a.drop(offset)];
	}

	*numbers{ |a, n, offset|
		n = n ? a.size - offset; //send n = nil to consume everything
		^[a.copyRange(offset, offset + n),offset + n]
	}

	*number{ |a,offset|
		^[a[offset]];
	}

	*buffer{ |a,server,offset|
		server = server ? Server.default ;
		^[Buffer.cachedBufferAt(server, a[offset]), offset + 1]
	}
}
