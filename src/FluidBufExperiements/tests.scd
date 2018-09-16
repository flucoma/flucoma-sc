s.reboot

//Quickie test of buffer allocation working
//Read a sound file
a = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
//size it appropriately (in our server code) and return the new object
f = FluidBufExperiments.allocMatch(s,a,rank:5);
//Make sure everything is kosher:
a.query
f.query


//Try full async version
s.reboot

//Quickie test of buffer allocation working
//Read a sound file
a = Buffer.read(s,"../../release-packaging/AudioFiles/Tremblay-AaS-SynthTwoVoices-M.wav".resolveRelative);
//Pass buffer to this, along with a rank. It will allocate a new buffer,
//size it appropriately (in our server code) and return the new object
f = FluidBufExperiments.allocMatchAsync(s,a,rank:5);
//Make sure everything is kosher:
a.query
f.query