// + FluidBufCompose {

	~concat = {
		arg x;
		x.postln;
	}
// }

~concat.value("ilove 10")



	~concat = {
		arg x;
	if(x.class != Array, {"Error".postln;}, {
		for (0,x.size - 1, {arg i;
			x[i].postln;
		});
	});
	}
// }

~concat.value("ilove 10")

~concat.value(b)


/////
