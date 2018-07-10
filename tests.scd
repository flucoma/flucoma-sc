s.quit;

s.boot;

// create a one-and-a-bit-cycle buffer and an empty one
b = Buffer.alloc(s,256);
c = Buffer.alloc(s,1000);

///resynthesis the source buffer with a slightly shorter wavelenght
b.sine2([256/250],[1],true,false);
b.fdNMF(c,4)
c.plot
//Voilà!

// clean up
b.free; c.free