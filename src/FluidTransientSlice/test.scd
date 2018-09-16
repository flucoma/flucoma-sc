s.reboot;

b = Buffer.read(s,"/Users/pa/Documents/documents@hudd/research/projects/fluid corpus navigation/research/archives-2017-18/denoise_stn/sources/01-mix.wav");

// basic param
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play //change to something sensible

// add null test