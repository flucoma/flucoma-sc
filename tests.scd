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

{PlayBuf.ar(5,c.bufnum,doneAction:2)[2].dup}.play


////////////////////////////
// test for osc clutter

SynthDef(\testcontrol, { arg fader = 0.5;
	Out.ar(0, Pulse.ar(MouseX.kr(-1).exprange(100,400), fader,0.1));
}).send(s);

x = Synth(\testcontrol);

y = {GrainBuf.ar(2,Impulse.ar(MouseX.kr(2,20)),MouseX.kr(1,0.1),b.bufnum,1,MouseY.kr(),2,LFNoise0.kr(20),-1,512,0.5)}.play;

z = {[0,BufRd.ar(1, b.bufnum, Phasor.ar(0, 2 , 0, BufFrames.kr(b.bufnum)),0)]}.play;

x.set(\fader, 0.05)
x.set(\fader, 0.5)

x.free; y.free; z.free;

// starting the buffer reading synths before is no issue, but cannot start accessing/playing them during the computing (lock?)
