s.quit
s.boot
// allocates a 16-sample buffer and fills it with ascending values
b = Buffer.read(s,"/Applications/Max.app/Contents/Resources/C74/media/msp/jongly.aif");
b.play;
b.plot;
c = Buffer.alloc(s,b.numFrames,2);

// run the code
b.fdNMF(c,2)

// read to check
c.plot;
{PlayBuf.ar(2,c.bufnum,doneAction:2)}.play


// play a sinewave in the background to see if the server freaks out
d = {SinOsc.ar(100,0,0.1)}.play
e = Buffer.alloc(s,b.numFrames,5);

// run the code
b.fdNMF(e,5, 150, 2048,1024,256)

// read to check
e.plot;
{Splay.ar(PlayBuf.ar(5,e.bufnum,doneAction:2))}.play