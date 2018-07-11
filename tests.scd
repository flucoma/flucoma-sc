s.quit
s.boot
// allocates a 16-sample buffer and fills it with ascending values
b = Buffer.alloc(s,50000);
b.sine1([0,0,0,0,0,0,0,0,0,1],true,false);
b.plot;
c = Buffer.alloc(s,50000);

// run the code
b.fdNMF(c);

// read to check
c.plot