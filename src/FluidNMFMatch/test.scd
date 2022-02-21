s.reboot;

    //from Fixed NMF example:
(
b = Buffer.read(s,FluidFilesPath("Tremblay-AaS-AcousticStrums-M.wav"));
c = Buffer.new(s);
x = Buffer.new(s);
e = Buffer.alloc(s,1,1);
y = Buffer.alloc(s,1,1);
)

    // train only 2 seconds
(
Routine {
    FluidBufNMF.process(s,b.bufnum,0,88200,0,1, c.bufnum, x.bufnum, rank:10);
    s.sync;
    c.query;
}.play;
)

// find the rank that has the picking sound by changing which channel to listen to
(
    ~element = 9;
    {PlayBuf.ar(10,c.bufnum)[~element]}.play
)

// copy all the other ranks on itself and the picking dictionnary as the sole component of the 1st channel
(
Routine{
            (0..9).remove(~element).do({|chan|FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA:chan, nChansA: 1, srcBufNumB: e.bufnum, dstBufNum: e.bufnum)});
    s.sync;
    e.query;
    s.sync;
    FluidBufCompose.process(s,srcBufNumA: x.bufnum, startChanA: ~element, nChansA: 1, srcBufNumB: e.bufnum, dstStartChanB: 1, dstBufNum: e.bufnum);
    s.sync;
    e.query;
}.play;
)

{DelayN.ar(PlayBuf.ar(1,b.bufnum),0.1,1024/44100, FluidNMFMatch.kr(PlayBuf.ar(1,b.bufnum),e.bufnum,2))}.play
