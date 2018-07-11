s.quit
s.boot
// allocates a 16-sample buffer and fills it with ascending values
b = Buffer.alloc(s,16);
c = Buffer.alloc(s,16,5);
b.setn(0, Array.series(16,-8,1));

// read to check
b.getn(0,16, {|msg| msg.postln});
c.getn(0,16, {|msg| msg.postln});

// run the code
b.fdNMF(c);

// read to check
b.getn(0,16, {|msg| msg.postln});
c.getn(0,16, {|msg| msg.postln});
