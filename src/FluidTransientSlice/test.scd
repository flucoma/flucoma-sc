s.reboot;

b = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);

// basic param
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play

// sexier params
x = {FluidTransients.ar(PlayBuf.ar(1,b.bufnum,loop:1))}.play //change to something sensible