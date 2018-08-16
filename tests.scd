s.quit
s.boot
////////////////////////////
// test for efficiency

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/denoise_stn/sources/01-mix.wav");
b.play;
c = Buffer.alloc(s,b.numFrames,5);
d = Main.elapsedTime; b.fdNMF(c,5, 100, 1024,1024,256,{e = Main.elapsedTime; (e-d).postln})

{(PlayBuf.ar(5,c.bufnum,doneAction:2).sum)+(-1*PlayBuf.ar(1,b.bufnum,doneAction:2))}.play
{Splay.ar(PlayBuf.ar(5,c.bufnum,doneAction:2))}.play

{PlayBuf.ar(5,c.bufnum,doneAction:2)[0].dup}.play